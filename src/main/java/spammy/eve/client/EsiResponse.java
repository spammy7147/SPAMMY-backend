package spammy.eve.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;

@Data
@Builder
public class EsiResponse {
    HttpHeaders headers;
    JsonNode body;
    boolean modified;
}
