package spammy.eve.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Data
@Builder
public class EsiResponse {
    HttpHeaders headers;
    List<JsonNode> body;
}
