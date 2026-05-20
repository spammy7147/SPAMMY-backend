package spammy.eve.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import spammy.eve.portfolio.domain.Character;
import spammy.eve.portfolio.repository.CharacterRepository;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EveTokenRefreshService {

    private final CharacterRepository characterRepository;
    private final RestClient restClient;

    @Value("${spammy.esi.client-id}")
    private String clientId;

    @Value("${spammy.esi.client-secret}")
    private String clientSecret;

    @Transactional
    public void refreshTokenIfNeeded(Character character) {
        if (!character.isTokenExpired()) {
            return;
        }

        log.info("Token for character {} is expired. Refreshing...", character.getCharacterName());

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", character.getRefreshToken());

        try {
            JsonNode response = restClient.post()
                    .uri("https://login.eveonline.com/v2/oauth/token")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("access_token")) {
                String newAccessToken = response.get("access_token").asString();
                String newRefreshToken = response.has("refresh_token") ? response.get("refresh_token").asString() : character.getRefreshToken();
                int expiresIn = response.has("expires_in") ? response.get("expires_in").asInt() : 1199;
                
                LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(expiresIn);

                character.updateToken(newAccessToken, newRefreshToken, newExpiresAt);
                characterRepository.save(character);
                log.info("Successfully refreshed token for character {}", character.getCharacterName());
            } else {
                log.error("Token refresh response is missing access_token for {}", character.getCharacterName());
            }
        } catch (Exception e) {
            log.error("Failed to refresh token for character {}: {}", character.getCharacterName(), e.getMessage());
            throw new RuntimeException("Token refresh failed", e);
        }
    }
}
