package spammy.eve.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import spammy.eve.global.aop.EsiCache;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    @EsiCache
    public EsiResponse get(String path, String accessToken) {
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

        return EsiResponse.builder()
                .headers(entity.getHeaders())
                .body(result)
                .isModified(true)
                .build();
    }

    /**
     * ESI POST 요청 처리 (토큰 갱신 등 FORM 형식)
     */
    @EsiCache
    public EsiResponse post(String path, MultiValueMap<String, String> body, String authHeader) {
        ResponseEntity<JsonNode> entity = restClient.post()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class);

        log.info("path : {}, body : {}",path, entity.getBody());
        return EsiResponse.builder()
                .headers(entity.getHeaders())
                .body(List.of(Objects.requireNonNull(entity.getBody())))
                .build();
    }

    private RestClient.RequestHeadersSpec<?> buildRequest(String path, String accessToken) {
        var request = restClient.get().uri(path.startsWith("http") ? path : ESI_BASE + path);
        if (accessToken != null) request = request.header("Authorization", "Bearer " + accessToken);
        return request;
    }
}
