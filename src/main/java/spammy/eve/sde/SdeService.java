package spammy.eve.sde;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    public boolean checkAndUpdate() {
        try {
            // 1. 저장된 ETag 조회
            String savedEtag = sdeVersionRepository.findTopByOrderByUpdatedAtDesc()
                    .map(SdeVersion::getEtag)
                    .orElse("");

            log.info("SDE 버전 체크 중... (저장된 ETag: {})", savedEtag);

            // 2. HEAD 요청으로 ETag만 먼저 확인 (파일 안 받음)
            ResponseEntity<Void> headResponse = restClient.head()
                    .uri(SDE_URL)
                    .retrieve()
                    .toBodilessEntity();

            String remoteEtag = headResponse.getHeaders().getFirst("ETag");

            if (savedEtag.equals(remoteEtag)) {
                log.info("SDE 최신 상태 유지 - 업데이트 불필요 (ETag: {})", remoteEtag);
                return false;
            }

            log.info("SDE 새 버전 감지 (ETag: {} → {})", savedEtag, remoteEtag);

            // 3. 실제 파일 다운로드
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(SDE_URL)
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] zipBytes = response.getBody();
            String newEtag = response.getHeaders().getFirst("ETag");
            String newLastModified = response.getHeaders().getFirst("Last-Modified");

            // 4. 적재
            reloadFromZip(zipBytes);

            // 5. 버전 저장
            sdeVersionRepository.save(SdeVersion.of(newEtag, newLastModified));

            log.info("SDE 업데이트 완료 (ETag: {})", newEtag);
            return true;

        } catch (Exception e) {
            log.error("SDE 업데이트 실패", e);
            return false;
        }
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

    private void reloadFromZip(byte[] zipBytes) {
        try {
            log.info("ZIP에서 파일 추출 중...");
            Map<String, byte[]> files = extractFromZip(zipBytes, "categories.jsonl", "groups.jsonl", "types.jsonl", "blueprints.jsonl");

            // 기존 데이터 삭제 (순서 중요 - FK 역순)
            log.info("기존 SDE 데이터 삭제 중...");
            blueprintItemRepository.deleteAllInBatch();
            bluePrintRepository.deleteAllInBatch();
            typeRepository.deleteAllInBatch();
            groupRepository.deleteAllInBatch();
            categoryRepository.deleteAllInBatch();

            parseCategories(files.get("categories.jsonl"));
            parseGroups(files.get("groups.jsonl"));
            parseTypes(files.get("types.jsonl"));
            parseBlueprints(files.get("blueprints.jsonl"));

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void parseCategories(byte[] data) throws Exception {
        List<Category> batch = new ArrayList<>();

        forEachNode(data, om(), root -> batch.add(Category.builder()
                .id(getLong(root, "_key"))
                .iconId(getLong(root, "iconID"))
                .nameEn(getString(root, "name", "en"))
                .nameKo(getString(root, "name", "ko"))
                .published(!Boolean.FALSE.equals(getBoolean(root, "published")))
                .build()));

        categoryRepository.saveAll(batch);
        log.info("Category 적재 완료 ({}건)", batch.size());
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
                    .build());
        });

        groupRepository.saveAll(batch);
        log.info("Group 적재 완료 ({}건)", batch.size());
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
                    .published(!Boolean.FALSE.equals(getBoolean(root, "published")))
                    .build());
        });

        typeRepository.saveAll(batch);
        log.info("Type 적재 완료 ({}건)", batch.size());
    }

    private void parseBlueprints(byte[] data) throws Exception {
        Map<Long, Type> typeCache = typeRepository.findAll()
                .stream().collect(Collectors.toMap(Type::getId, t -> t));
        List<Blueprint> batch = new ArrayList<>();

        forEachNode(data, om(), root -> {
            Long id = getLong(root, "_key");
            Integer limit = getInt(root, "maxProductionLimit");

            Iterator<String> activities = root.path("activities").propertyNames().iterator();
            while (activities.hasNext()) {
                String activityName = activities.next();
                ActivityType activityType;
                try {
                    activityType = ActivityType.valueOf(activityName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return;
                }
                JsonNode activity = root.path("activities").path(activityName);
                buildBlueprint(id, limit, activityType, activity, typeCache)
                        .ifPresent(batch::add);
            }
        });

        bluePrintRepository.saveAll(batch);
        log.info("Blueprint 적재 완료 ({}건)", batch.size());
    }

    private Optional<Blueprint> buildBlueprint(Long id, Integer limit, ActivityType activityType,
                                               JsonNode activity, Map<Long, Type> typeCache) {
        Blueprint blueprint = Blueprint.builder()
                .blueprintTypeId(id)
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
                    .probability(null)
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