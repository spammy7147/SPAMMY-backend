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

    public void checkAndUpdate() {
        try {
            // 1. 저장된 빌드 넘버 조회
            Long savedBuildNumber = sdeVersionRepository.findTopByOrderByUpdatedAtDesc()
                    .map(SdeVersion::getBuildNumber)
                    .orElse(0L);

            log.info("SDE 버전 체크 중... (저장된 Build: {})", savedBuildNumber);

            // 2. latest.jsonl 에서 최신 빌드 넘버  확인
            JsonNode latestJsonl = restClient.get().uri(LATEST_URL).retrieve().body(JsonNode.class);
            if (latestJsonl == null) return;

            Long remoteBuildNumber = latestJsonl.path("buildNumber").asLong();

            if (savedBuildNumber.equals(remoteBuildNumber)) {
                log.info("SDE 최신 상태 유지 (Build: {})", remoteBuildNumber);
                return;
            }

            log.info("SDE 새 빌드 감지 ({} -> {})", savedBuildNumber, remoteBuildNumber);

            // 3. 변경점(Changes) 확인하여 업데이트 필요 여부 결정
            SdeChanges changes = null;
            if (savedBuildNumber > 0) {
                try {
                    String changesUrl = CHANGES_URL_PREFIX + remoteBuildNumber + ".jsonl";
                    String changesJsonl = restClient.get().uri(changesUrl).retrieve().body(String.class);

                    if (changesJsonl != null) {
                        JsonNode metaNode = om().readTree(changesJsonl.split("\n")[0]);
                        Long lastBuildNumber = metaNode.path("lastBuildNumber").asLong();

                        // 연속된 빌드인 경우에만 변경점 필터링 적용
                        if (savedBuildNumber.equals(lastBuildNumber)) {
                            changes = extractChanges(changesJsonl);
                        } else {
                            log.info("이전 빌드({})와 저장된 빌드({})가 연속되지 않아 전체 업데이트 검토", lastBuildNumber, savedBuildNumber);
                        }
                    }
                } catch (Exception e) {
                    log.warn("변경점 확인 중 오류 발생, 전체 업데이트 진행: {}", e.getMessage());
                }
            }

            if (changes != null && !changes.hasChanges()) {
                log.info("관련 도메인(Category/Group/Type/Blueprint) 변경 없음. 빌드 번호만 업데이트.");
                sdeVersionRepository.save(SdeVersion.of("SKIP", "SKIP", remoteBuildNumber));
                return;
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

            reloadFromZip(zipBytes, changes);

            // 5. 버전 저장
            sdeVersionRepository.save(SdeVersion.of(newEtag, newLastModified, remoteBuildNumber));

            log.info("SDE 업데이트 완료 (Build: {})", remoteBuildNumber);

        } catch (Exception e) {
            log.error("SDE 업데이트 실패", e);
        }
    }

    private record SdeChanges(
            Set<Long> updatedCategories, Set<Long> deletedCategories,
            Set<Long> updatedGroups, Set<Long> deletedGroups,
            Set<Long> updatedTypes, Set<Long> deletedTypes,
            Set<Long> updatedBlueprints, Set<Long> deletedBlueprints
    ) {
        public boolean hasChanges() {
            return !updatedCategories.isEmpty() || !deletedCategories.isEmpty() ||
                    !updatedGroups.isEmpty() || !deletedGroups.isEmpty() ||
                    !updatedTypes.isEmpty() || !deletedTypes.isEmpty() ||
                    !updatedBlueprints.isEmpty() || !deletedBlueprints.isEmpty();
        }
    }

    private SdeChanges extractChanges(String jsonl) throws Exception {
        Set<Long> upCategory = new HashSet<>(), delCategory = new HashSet<>();
        Set<Long> upGroup = new HashSet<>(), delGroup = new HashSet<>();
        Set<Long> upType = new HashSet<>(), delType = new HashSet<>();
        Set<Long> upBlueprint = new HashSet<>(), delBlueprint = new HashSet<>();

        String[] lines = jsonl.split("\n");
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("{ \"_key\": \"_meta\"")) continue;
            JsonNode node = om().readTree(line);
            String key = node.path("_key").asString();

            switch (key) {
                case "categories" -> collectIds(node, upCategory, delCategory);
                case "groups" -> collectIds(node, upGroup, delGroup);
                case "types" -> collectIds(node, upType, delType);
                case "blueprints" -> collectIds(node, upBlueprint, delBlueprint);
            }
        }
        return new SdeChanges(upCategory, delCategory, upGroup, delGroup, upType, delType, upBlueprint, delBlueprint);
    }

    private void collectIds(JsonNode node, Set<Long> updateSet, Set<Long> deleteSet) {
        if (node.has("added")) node.path("added").forEach(id -> updateSet.add(id.asLong()));
        if (node.has("changed")) node.path("changed").forEach(id -> updateSet.add(id.asLong()));
        if (node.has("changedLocalization")) node.path("changedLocalization").forEach(id -> updateSet.add(id.asLong()));
        if (node.has("deleted")) node.path("deleted").forEach(id -> deleteSet.add(id.asLong()));
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

    private void reloadFromZip(byte[] zipBytes, SdeChanges changes) throws Exception {
        log.info("ZIP에서 파일 추출 중...");
        Map<String, byte[]> files = extractFromZip(zipBytes, "categories.jsonl", "groups.jsonl", "types.jsonl", "blueprints.jsonl");

        if (changes == null) {
            log.info("이전 빌드 정보를 찾을 수 없거나 버전이 누락되어 전체 업데이트를 수행합니다. 기존 데이터를 초기화합니다.");
            blueprintItemRepository.deleteAllInBatch();
            bluePrintRepository.deleteAllInBatch();
            typeRepository.deleteAllInBatch();
            groupRepository.deleteAllInBatch();
            categoryRepository.deleteAllInBatch();
        }

        log.info("SDE 업데이트 시작 ({} 방식)", (changes == null ? "전체" : "증분"));
        
        parseCategories(files.get("categories.jsonl"),
                changes != null ? changes.updatedCategories() : null,
                changes != null ? changes.deletedCategories() : null);

        parseGroups(files.get("groups.jsonl"),
                changes != null ? changes.updatedGroups() : null,
                changes != null ? changes.deletedGroups() : null);

        parseTypes(files.get("types.jsonl"),
                changes != null ? changes.updatedTypes() : null,
                changes != null ? changes.deletedTypes() : null);

        parseBlueprints(files.get("blueprints.jsonl"),
                changes != null ? changes.updatedBlueprints() : null,
                changes != null ? changes.deletedBlueprints() : null);
    }

    private void parseCategories(byte[] data, Set<Long> updateIds, Set<Long> deleteIds) throws Exception {
        // 증분 업데이트인 경우에만 선택적 삭제 수행 (전체 업데이트는 이미 초기화됨)
        if (updateIds != null && deleteIds != null && !deleteIds.isEmpty()) {
            categoryRepository.deleteAllById(deleteIds);
        }

        List<Category> batch = new ArrayList<>();
        forEachNode(data, om(), root -> {
            Long id = getLong(root, "_key");
            // 증분 업데이트인 경우 업데이트 대상 ID가 아니면 스킵
            if (updateIds != null && !updateIds.contains(id)) return;

            batch.add(Category.builder()
                    .id(id)
                    .iconId(getLong(root, "iconID"))
                    .nameEn(getString(root, "name", "en"))
                    .nameKo(getString(root, "name", "ko"))
                    .published(getBoolean(root, "published"))
                    .build());
        });

        categoryRepository.saveAll(batch);
        log.info("Category 업데이트 완료 ({}건)", batch.size());
    }

    private void parseGroups(byte[] data, Set<Long> updateIds, Set<Long> deleteIds) throws Exception {
        if (updateIds != null && deleteIds != null && !deleteIds.isEmpty()) {
            groupRepository.deleteAllById(deleteIds);
        }

        Map<Long, Category> categoryCache = categoryRepository.findAll()
                .stream().collect(Collectors.toMap(Category::getId, c -> c));
        List<Group> batch = new ArrayList<>();

        forEachNode(data, om(), root -> {
            Long id = getLong(root, "_key");
            if (updateIds != null && !updateIds.contains(id)) return;

            Category category = categoryCache.get(getLong(root, "categoryID"));
            if (category == null) return;

            batch.add(Group.builder()
                    .id(id)
                    .category(category)
                    .nameEn(getString(root, "name", "en"))
                    .nameKo(getString(root, "name", "ko"))
                    .published(getBoolean(root, "published"))
                    .build());
        });

        groupRepository.saveAll(batch);
        log.info("Group 업데이트 완료 ({}건)", batch.size());
    }

    private void parseTypes(byte[] data, Set<Long> updateIds, Set<Long> deleteIds) throws Exception {
        if (updateIds != null && deleteIds != null && !deleteIds.isEmpty()) {
            typeRepository.deleteAllById(deleteIds);
        }

        Map<Long, Group> groupCache = groupRepository.findAll()
                .stream().collect(Collectors.toMap(Group::getId, g -> g));
        List<Type> batch = new ArrayList<>();

        forEachNode(data, om(), root -> {
            Long id = getLong(root, "_key");
            if (updateIds != null && !updateIds.contains(id)) return;

            Group group = groupCache.get(getLong(root, "groupID"));
            if (group == null) return;

            batch.add(Type.builder()
                    .id(id)
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
        log.info("Type 업데이트 완료 ({}건)", batch.size());
    }

    private void parseBlueprints(byte[] data, Set<Long> updateIds, Set<Long> deleteIds) throws Exception {
        // 증분 업데이트 상황에서의 삭제 처리
        if (updateIds != null) {
            if (deleteIds != null && !deleteIds.isEmpty()) {
                blueprintItemRepository.deleteByBlueprintTypeIdIn(deleteIds);
                bluePrintRepository.deleteByBlueprintTypeIdIn(deleteIds);
            }
            if (!updateIds.isEmpty()) {
                // 업데이트 대상도 기존 데이터 삭제 후 재삽입
                blueprintItemRepository.deleteByBlueprintTypeIdIn(updateIds);
                bluePrintRepository.deleteByBlueprintTypeIdIn(updateIds);
            }
        }

        Map<Long, Type> typeCache = typeRepository.findAll()
                .stream().collect(Collectors.toMap(Type::getId, t -> t));

        List<Blueprint> batch = new ArrayList<>();
        forEachNode(data, om(), root -> {
            Long typeId = getLong(root, "_key");
            if (updateIds != null && !updateIds.contains(typeId)) return;

            Integer limit = getInt(root, "maxProductionLimit");
            for (String activityName : root.path("activities").propertyNames()) {
                ActivityType activityType;
                try {
                    activityType = ActivityType.valueOf(activityName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }

                JsonNode activityNode = root.path("activities").path(activityName);
                buildBlueprint(null, typeId, limit, activityType, activityNode, typeCache)
                        .ifPresent(batch::add);
            }
        });

        bluePrintRepository.saveAll(batch);
        log.info("Blueprint 업데이트 완료 ({}건)", batch.size());
    }

    private Optional<Blueprint> buildBlueprint(Long existingId, Long typeId, Integer limit, ActivityType activityType,
                                               JsonNode activity, Map<Long, Type> typeCache) {
        Blueprint blueprint = Blueprint.builder()
                .id(existingId)
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