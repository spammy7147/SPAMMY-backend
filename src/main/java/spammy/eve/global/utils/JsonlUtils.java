package spammy.eve.global.utils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * JSONL 파일의 각 라인을 JsonNode로 변환하여 데이터를 추출하는 유틸리티 클래스입니다.
 * 모든 데이터 추출 메서드는 값이 없거나 null인 경우 null을 반환합니다.
 */
public final class JsonlUtils {
    private JsonlUtils() {}

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    /**
     * JSONL 데이터의 각 라인을 순회하며 JsonNode를 처리합니다.
     */
    public static void forEachNode(byte[] data, ObjectMapper om, ThrowingConsumer<JsonNode> consumer) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                consumer.accept(om.readTree(line));
            }
        }
    }

    /**
     * 지정된 경로의 하위 노드를 탐색합니다. 경로가 없거나 노드가 null이면 null을 반환합니다.
     */
    private static JsonNode getChildNode(JsonNode node, String... fields) {
        if (node == null) return null;
        JsonNode current = node;
        if (fields != null) {
            for (String field : fields) {
                current = current.path(field);
                if (current.isMissingNode() || current.isNull()) return null;
            }
        }
        return current;
    }

    public static Long getLong(JsonNode node, String... fields) {
        JsonNode target = getChildNode(node, fields);
        return (target == null) ? null : target.asLong();
    }

    public static Integer getInt(JsonNode node, String... fields) {
        JsonNode target = getChildNode(node, fields);
        return (target == null) ? null : target.asInt();
    }

    public static String getString(JsonNode node, String... fields) {
        JsonNode target = getChildNode(node, fields);
        return (target == null) ? "" : target.asString();
    }

    public static Boolean getBoolean(JsonNode node, String... fields) {
        JsonNode target = getChildNode(node, fields);
        return target != null && target.asBoolean();
    }

    public static Double getDouble(JsonNode node, String... fields) {
        JsonNode target = getChildNode(node, fields);
        return (target == null) ? null : target.asDouble();
    }
}
