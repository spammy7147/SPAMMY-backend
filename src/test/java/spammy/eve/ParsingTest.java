package spammy.eve;

import spammy.eve.domain.blueprint.BlueprintItem;
import spammy.eve.domain.blueprint.BlueprintItemKind;
import spammy.eve.domain.sde.Type;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestConstructor;
import spammy.eve.domain.*;
import spammy.eve.repository.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParsingTest {

    private final BlueprintRepository bluePrintRepository;
    private final CategoryRepository categoryRepository;
    private final GroupRepository groupRepository;
    private final TypeRepository typeRepository;
    private final BlueprintItemRepository blueprintItemRepository;

    ParsingTest(BlueprintRepository bluePrintRepository,
                CategoryRepository categoryRepository,
                GroupRepository groupRepository,
                TypeRepository typeRepository,
                BlueprintItemRepository blueprintItemRepository) {
        this.bluePrintRepository = bluePrintRepository;
        this.categoryRepository = categoryRepository;
        this.groupRepository = groupRepository;
        this.typeRepository = typeRepository;
        this.blueprintItemRepository = blueprintItemRepository;
    }

    private ObjectMapper om() {
        return JsonMapper.builder().build();
    }

    @Test
    @Rollback(false)
    @Order(1)
    void parseCategories() throws Exception {
        ObjectMapper om = om();
        ClassPathResource resource = new ClassPathResource("static/categories.jsonl");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                JsonNode root = om.readTree(line);

                Category category = Category.builder()
                        .id(root.path("_key").asLong())
                        .iconId(root.path("iconID").asLong())
                        .nameEn(root.path("name").path("en").asText())
                        .nameKo(root.path("name").path("ko").asText())
                        .published(root.path("published").asBoolean())
                        .build();

                categoryRepository.save(category);
            }
        }
    }

    @Test
    @Rollback(false)
    @Order(2)
    void parseGroups() throws Exception {
        ObjectMapper om = om();
        ClassPathResource resource = new ClassPathResource("static/groups.jsonl");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                JsonNode root = om.readTree(line);

                Group group = Group.builder()
                        .id(root.path("_key").asLong())
                        .category(categoryRepository.findById(root.path("categoryID").asLong()).orElse(null))
                        .nameEn(root.path("name").path("en").asText())
                        .nameKo(root.path("name").path("ko").asText())
                        .build();

                groupRepository.save(group);
            }
        }
    }

    @Test
    @Rollback(false)
    @Order(3)
    void parseTypes() throws Exception {
        ObjectMapper om = om();
        ClassPathResource resource = new ClassPathResource("static/types.jsonl");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                JsonNode root = om.readTree(line);

                Type type = Type.builder()
                        .id(root.path("_key").asLong())
                        .group(groupRepository.findById(root.path("groupID").asLong()).orElse(null))
                        .nameEn(root.path("name").path("en").asText())
                        .nameKo(root.path("name").path("ko").asText())
                        .published(root.path("published").asBoolean())
                        .build();

                typeRepository.save(type);
            }
        }
    }

    @Test
    @Rollback(false)
    @Order(4)
    void parseBlueprints() throws Exception {
        ObjectMapper om = om();

        // ✅ Type 전체 캐싱 - DB 조회 수십만 번 → 1번
        Map<Long, Type> typeCache = typeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Type::getId, t -> t));

        ClassPathResource resource = new ClassPathResource("static/blueprints.jsonl");
        List<Blueprint> batch = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                JsonNode root = om.readTree(line);
                Long id = root.path("_key").asLong();
                Integer limit = root.has("maxProductionLimit") ? root.path("maxProductionLimit").asInt() : null;

                Iterator<String> activities = root.path("activities").propertyNames().iterator();
                while (activities.hasNext()) {
                    String activityName = activities.next();
                    ActivityType activityType;
                    try {
                        activityType = ActivityType.valueOf(activityName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    JsonNode activity = root.path("activities").path(activityName);
                    Integer time = activity.has("time") ? activity.path("time").asInt() : null;

                    Blueprint blueprint = Blueprint.builder()
                            .blueprintTypeId(id)
                            .activityType(activityType)
                            .timeSeconds(time)
                            .maxProductionLimit(limit)
                            .items(new ArrayList<>())
                            .build();

                    boolean skip = false;
                    List<BlueprintItem> pendingItems = new ArrayList<>();

                    for (JsonNode material : activity.path("materials")) {
                        Type type = typeCache.get(material.path("typeID").asLong());
                        if (type == null) { skip = true; break; }
                        pendingItems.add(BlueprintItem.builder()
                                .blueprint(blueprint)
                                .kind(BlueprintItemKind.MATERIAL)
                                .type(type)
                                .qty(material.path("quantity").asLong())
                                .consumed(true)
                                .probability(null)
                                .build());
                    }

                    if (!skip) {
                        for (JsonNode product : activity.path("products")) {
                            Type type = typeCache.get(product.path("typeID").asLong());
                            if (type == null) { skip = true; break; }
                            pendingItems.add(BlueprintItem.builder()
                                    .blueprint(blueprint)
                                    .kind(BlueprintItemKind.PRODUCT)
                                    .type(type)
                                    .qty(product.path("quantity").asLong())
                                    .consumed(false)
                                    .probability(product.has("probability") ? product.path("probability").asDouble() : null)
                                    .build());
                        }
                    }

                    if (skip) continue;

                    pendingItems.forEach(blueprint::addItem);
                    batch.add(blueprint);

                    if (batch.size() >= 500) {
                        bluePrintRepository.saveAll(batch);
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) bluePrintRepository.saveAll(batch);
        }
    }
}