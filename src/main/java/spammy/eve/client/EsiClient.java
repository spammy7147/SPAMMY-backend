package spammy.eve.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import spammy.eve.global.aop.EsiCache;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class EsiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String ESI_BASE = "https://esi.evetech.net/latest";

    /**
     * ESI GET 요청. Aspect에서 주입한 ETag와 함께 ESI를 호출함.
     */
    @EsiCache
    public EsiResponse get(String path, String accessToken, String etag) {
        ResponseEntity<JsonNode> entity = buildRequest(path, accessToken)
                .header(HttpHeaders.IF_NONE_MATCH, etag)
                .retrieve()
                .onStatus(status -> status.value() == 304, (req, res) -> { /* 304 Not Modified */ })
                .toEntity(JsonNode.class);

        JsonNode body = (entity.getStatusCode() == HttpStatus.OK) 
                ? processPagination(path, accessToken, entity) 
                : null;

        return EsiResponse.builder()
                .headers(entity.getHeaders())
                .body(body)
                .modified(entity.getStatusCode() == HttpStatus.OK)
                .build();
    }

    private JsonNode processPagination(String path, String accessToken, ResponseEntity<JsonNode> firstPage) {
        List<JsonNode> result = new ArrayList<>();
        if (firstPage.getBody() != null) result.add(firstPage.getBody());

        String pagesHeader = firstPage.getHeaders().getFirst("x-pages");
        if (pagesHeader != null) {
            int totalPages = Integer.parseInt(pagesHeader);
            String separator = path.contains("?") ? "&" : "?";
            IntStream.rangeClosed(2, totalPages).forEach(i -> {
                JsonNode pageBody = buildRequest(path + separator + "page=" + i, accessToken)
                        .retrieve()
                        .body(JsonNode.class);
                if (pageBody != null) result.add(pageBody);
            });
        }

        if (result.size() == 1) return result.getFirst();

        ArrayNode combinedNode = objectMapper.createArrayNode();
        for (JsonNode page : result) {
            if (page.isArray()) combinedNode.addAll((ArrayNode) page);
            else combinedNode.add(page);
        }
        return combinedNode;
    }

    /**
     * ESI POST 요청 처리 (기본 형식)
     */
    @EsiCache
    public EsiResponse post(String path, Object body, String accessToken) {
        var request = restClient.post()
                .uri(path.startsWith("http") ? path : ESI_BASE + path);

        if (accessToken != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }

        if (body instanceof MultiValueMap) {
            request.contentType(MediaType.APPLICATION_FORM_URLENCODED);
        } else {
            request.contentType(MediaType.APPLICATION_JSON);
        }

        ResponseEntity<JsonNode> entity = request
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class);

        return EsiResponse.builder()
                .headers(entity.getHeaders())
                .body(entity.getBody())
                .modified(true)
                .build();
    }

    private RestClient.RequestHeadersSpec<?> buildRequest(String path, String accessToken) {
        var request = restClient.get().uri(path.startsWith("http") ? path : ESI_BASE + path);
        if (accessToken != null) request = request.header("Authorization", "Bearer " + accessToken);
        return request;
    }
}
