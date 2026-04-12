package spammy.eve.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class EsiClient {

    private final RestClient restClient;

    private static final String ESI_BASE = "https://esi.evetech.net/latest";

    /**
     * ESI GET 요청. 각 페이지의 응답 body를 리스트로 반환.
     */
    public List<JsonNode> get(String path, String accessToken) {
        List<JsonNode> result = new ArrayList<>();

        ResponseEntity<JsonNode> entity = buildRequest(path, accessToken)
                .retrieve()
                .toEntity(JsonNode.class);

        JsonNode body = entity.getBody();
        if (body != null) result.add(body);

        String pagesHeader = entity.getHeaders().getFirst("x-pages");
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

        return result;
    }

    /**
     * ESI POST 요청 처리 (토큰 갱신 등 FORM 형식)
     */
    public JsonNode post(String url, MultiValueMap<String, String> body, String authHeader) {
        return restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * ESI POST 요청 처리 (JSON 바디 형식)
     */
    public JsonNode postJson(String path, Object body) {
        return restClient.post()
                .uri(ESI_BASE + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private RestClient.RequestHeadersSpec<?> buildRequest(String path, String accessToken) {
        var request = restClient.get().uri(path.startsWith("http") ? path : ESI_BASE + path);
        if (accessToken != null) request = request.header("Authorization", "Bearer " + accessToken);
        return request;
    }
}
