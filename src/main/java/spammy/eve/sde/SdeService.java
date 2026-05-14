package spammy.eve.sde;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import spammy.eve.character.domain.ActivityType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static spammy.eve.global.utils.JsonlUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SdeService {

    private final SdeVersionRepository sdeVersionRepository;
    private final RestClient restClient;
    private final CategoryRepository categoryRepository;
    private final GroupRepository groupRepository;
    private final TypeRepository typeRepository;
    private final BlueprintRepository bluePrintRepository;
    private final BlueprintItemRepository blueprintItemRepository;

    private ObjectMapper om() {
        return JsonMapper.builder().build();
    }

    private static final String SDE_URL = "https://developers.eveonline.com/static-data/eve-online-static-data-latest-jsonl.zip";
    private static final String LATEST_URL = "https://developers.eveonline.com/static-data/tranquility/latest.jsonl";
    private static final String CHANGES_URL_PREFIX = "https://developers.eveonline.com/static-data/tranquility/changes/";

    public boolean checkAndUpdate() {
        try {
            // 1. 저장된 빌드 넘버 조회
            Long savedBuildNumber = sdeVersionRepository.findTopByOrderByUpdatedAtDesc()
                    .map(SdeVersion::getBuildNumber)
                    .orElse(0L);

            log.info("SDE 버전 체크 중... (저장된 Build: {})", savedBuildNumber);

            // 2. latest.jsonl 에서 최신 빌드 넘버 확인
            String latestJsonl = restClient.get().uri(LATEST_URL).retrieve().body(String.class);
            if (latestJsonl == null) return false;
            
            JsonNode latestNode = om().readTree(latestJsonl);
            Long remoteBuildNumber = latestNode.path("buildNumber").asLong();

            if (savedBuildNumber.equals(remoteBuildNumber)) {
                log.info("SDE 최신 상태 유지 (Build: {})", remoteBuildNumber);
                return false;
            }

            log.info("SDE 새 빌드 감지 ({} -> {})", savedBuildNumber, remoteBuildNumber);

            // 3. 변경점(Changes) 확인하여 업데이트 필요 여부 결정
            boolean needsUpdate = true;
            if (savedBuildNumber > 0) {
                try {
                    String changesUrl = CHANGES_URL_PREFIX + remoteBuildNumber + ".jsonl";
                    String changesJsonl = restClient.get().uri(changesUrl).retrieve().body(String.class);
                    
                    if (changesJsonl != null) {
                        JsonNode metaNode = om().readTree(changesJsonl.split("\n")[0]);
                        Long lastBuildNumber = metaNode.path("lastBuildNumber").asLong();

                        // 연속된 빌드인 경우에만 변경점 필터링 적용
                        if (savedBuildNumber.equals(lastBuildNumber)) {
                            needsUpdate = isRelevantChangeFound(changesJsonl);
                        } else {
                            log.info("이전 빌드({})와 저장된 빌드({})가 연속되지 않아 전체 업데이트 검토", lastBuildNumber, savedBuildNumber);
                        }
                    }
                } catch (Exception e) {
                    log.warn("변경점 확인 중 오류 발생, 전체 업데이트 진행: {}", e.getMessage());
                }
            }

            if (!needsUpdate) {
                log.info("관련 도메인(Category/Group/Type/Blueprint) 변경 없음. 빌드 번호만 업데이트.");
                sdeVersionRepository.save(SdeVersion.of("SKIP", "SKIP", remoteBuildNumber));
                return true;
            }

            // 4. 실제 파일 다운로드 및 적재
            log.info("관련 변경 사항 확인됨. SDE ZIP 다운로드 시작...");
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(SDE_URL)
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] zipBytes = response.getBody();
            String newEtag = response.getHeaders().getFirst("ETag");
            String newLastModified = response.getHeaders().getFirst("Last-Modified");

            reloadFromZip(zipBytes);

            // 5. 버전 저장
            sdeVersionRepository.save(SdeVersion.of(newEtag, newLastModified, remoteBuildNumber));

            log.info("SDE 업데이트 완료 (Build: {})", remoteBuildNumber);
            return true;

        } catch (Exception e) {
            log.error("SDE 업데이트 실패", e);
            return false;
        }
    }

    private boolean isRelevantChangeFound(String jsonl) throws Exception {
        Set<String> relevantKeys = Set.of("categories", "groups", "types", "blueprints");
        String[] lines = jsonl.split("\n");
        for (String line : lines) {
            if (line.isBlank()) continue;
            JsonNode node = om().readTree(line);
            String key = node.path("_key").asString();
            if (relevantKeys.contains(key)) {
                if (node.has("added") || node.has("changed") || node.has("deleted") || node.has("changedLocalization")) {
                    log.info("변경 감지된 도메인: {}", key);
                    return true;
                }
            }
        }
        return false;
    }

    // ZIP에서 필요한 파일만 추출
    private Map<String, byte[]> extractFromZip(byte[] zipBytes, String... fileNames) throws Exception {
        Set<String> targets = new HashSet<>(Arrays.asList(fileNames));
        Map<String, byte[]> result = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = new File(entry.getName()).getName(); // 경로 제거
                if (targets.contains(name)) {
                    result.put(name, zis.readAllBytes());
                    log.info("ZIP 추출 완료: {}", name);
                }
                zis.closeEntry();
            }
        }
        return result;
    }

    private void reloadFromZip(byte[] zipBytes) throws Exception {
        log.info("ZIP에서 파일 추출 중...");
        Map<String, byte[]> files = extractFromZip(zipBytes, "categories.jsonl", "groups.jsonl", "types.jsonl", "blueprints.jsonl");

        log.info("SDE 증분 업데이트 시작...");
        parseCategories(files.get("categories.jsonl"));
        parseGroups(files.get("groups.jsonl"));
        parseTypes(files.get("types.jsonl"));
        parseBlueprints(files.get("blueprints.jsonl"));
    }

    private void parseCategories(byte[] data) throws Exception {
        List<Category> batch = new ArrayList<>();
        forEachNode(data, om(), root -> batch.add(Category.builder()
                .id(getLong(root, "_key"))
                .iconId(getLong(root, "iconID"))
                .nameEn(getString(root, "name", "en"))
                .nameKo(getString(root, "name", "ko"))
                .published(getBoolean(root, "published"))
                .build()));

        categoryRepository.saveAll(batch);
        log.info("Category 증분 업데이트 완료 ({}건)", batch.size());
    }

    private void parseGroups(byte[] data) throws Exception {
        Map<Long, Category> categoryCache = categoryRepository.findAll()
                .stream().collect(Collectors.toMap(Category::getId, c -> c));
        List<Group> batch = new ArrayList<>();

        forEachNode(data, om(), root -> {
            Category category = categoryCache.get(getLong(root, "categoryID"));
            if (category == null) return;

            batch.add(Group.builder()
                    .id(getLong(root, "_key"))
                    .category(category)
                    .nameEn(getString(root, "name", "en"))
                    .nameKo(getString(root, "name", "ko"))
                    .published(getBoolean(root, "published"))
                    .build());
        });

        groupRepository.saveAll(batch);
        log.info("Group 증분 업데이트 완료 ({}건)", batch.size());
    }

    private void parseTypes(byte[] data) throws Exception {
        Map<Long, Group> groupCache = groupRepository.findAll()
                .stream().collect(Collectors.toMap(Group::getId, g -> g));
        List<Type> batch = new ArrayList<>();

        forEachNode(data, om(), root -> {
            Group group = groupCache.get(getLong(root, "groupID"));
            if (group == null) return;

            batch.add(Type.builder()
                    .id(getLong(root, "_key"))
                    .group(group)
                    .nameEn(getString(root, "name", "en"))
                    .nameKo(getString(root, "name", "ko"))
                    .published(getBoolean(root, "published"))
                    .portionSize(getInt(root, "portionSize"))
                    .volume(getDouble(root, "volume"))
                    .marketGroupId(getLong(root, "marketGroupID"))
                    .build());
        });

        typeRepository.saveAll(batch);
        log.info("Type 증분 업데이트 완료 ({}건)", batch.size());
    }

    private void parseBlueprints(byte[] data) throws Exception {
        // BlueprintItem은 복합 키 유니크 제약이 있어 증분 업데이트 시 충돌이 잦으므로,
        // 전체 데이터를 재적재하기 전에 기존 Item들을 먼저 삭제함.
        blueprintItemRepository.deleteAllInBatch();

        Map<Long, Type> typeCache = typeRepository.findAll()
                .stream().collect(Collectors.toMap(Type::getId, t -> t));
        
        // Blueprint는 복합키 개념(typeId + activity)이므로 관리가 까다로움. 
        // 여기서는 단순화를 위해 blueprint_type_id 기준으로 기존 데이터를 조회하여 매칭하거나, 
        // 중복 방지를 위해 기존 데이터를 삭제 후 재삽입하는 방식 등을 고려할 수 있으나,
        // 사용자 요청에 따라 'row 자체를 바꿔치는' 방식(SaveAll upsert)을 유지하되 
        // Blueprint의 경우 activity별로 unique하므로 기존 데이터를 activity별로 관리해야 함.
        
        // 기존 Blueprint & Items 삭제 (증분 처리가 가장 복잡한 영역이므로 Blueprint는 일단 초기화 후 재삽입 방식 유지 권장되나, 
        // 전체 삭제를 피하기 위해 blueprint_type_id 단위로 처리하는 것이 이상적임)
        // 일단은 전체 삭제 대신 saveAll(upsert)을 시도하되, Blueprint의 ID(PK) 관리가 필요함.
        
        // 효율적인 처리를 위해 Blueprint는 비즈니스 키(blueprintTypeId, activityType)로 기존 ID를 맵핑
        Map<String, Long> existingBpIds = bluePrintRepository.findAll().stream()
                .collect(Collectors.toMap(
                        b -> b.getBlueprintTypeId() + ":" + b.getActivityType(),
                        Blueprint::getId
                ));

        List<Blueprint> batch = new ArrayList<>();
        forEachNode(data, om(), root -> {
            Long typeId = getLong(root, "_key");
            Integer limit = getInt(root, "maxProductionLimit");

            for (String activityName : root.path("activities").propertyNames()) {
                ActivityType activityType;
                try {
                    activityType = ActivityType.valueOf(activityName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }

                JsonNode activityNode = root.path("activities").path(activityName);
                String key = typeId + ":" + activityType;
                Long existingId = existingBpIds.get(key);

                buildBlueprint(existingId, typeId, limit, activityType, activityNode, typeCache)
                        .ifPresent(batch::add);
            }
        });

        bluePrintRepository.saveAll(batch);
        log.info("Blueprint 증분 업데이트 완료 ({}건)", batch.size());
    }

    private Optional<Blueprint> buildBlueprint(Long existingId, Long typeId, Integer limit, ActivityType activityType,
                                               JsonNode activity, Map<Long, Type> typeCache) {
        Blueprint blueprint = Blueprint.builder()
                .id(existingId) // 기존 ID가 있으면 할당하여 Update 유도
                .blueprintTypeId(typeId)
                .activityType(activityType)
                .timeSeconds(getInt(activity, "time"))
                .maxProductionLimit(limit)
                .items(new ArrayList<>())
                .build();

        List<BlueprintItem> pendingItems = new ArrayList<>();

        for (JsonNode material : activity.path("materials")) {
            Type type = typeCache.get(material.path("typeID").asLong());
            if (type == null) return Optional.empty();
            pendingItems.add(BlueprintItem.builder()
                    .blueprint(blueprint)
                    .kind(BlueprintItemKind.MATERIAL)
                    .type(type)
                    .qty(getLong(material, "quantity"))
                    .consumed(true)
                    .build());
        }

        for (JsonNode product : activity.path("products")) {
            Type type = typeCache.get(product.path("typeID").asLong());
            if (type == null) return Optional.empty();
            pendingItems.add(BlueprintItem.builder()
                    .blueprint(blueprint)
                    .kind(BlueprintItemKind.PRODUCT)
                    .type(type)
                    .qty(getLong(product, "quantity"))
                    .probability(getDouble(product, "probability"))
                    .consumed(false)
                    .build());
        }

        pendingItems.forEach(blueprint::addItem);
        return Optional.of(blueprint);
    }

}