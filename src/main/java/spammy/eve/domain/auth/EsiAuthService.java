package spammy.eve.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import spammy.eve.client.EsiClient;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.character.CharacterRepository;
import spammy.eve.domain.user.User;
import spammy.eve.domain.user.UserRepository;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import static spammy.eve.global.utils.JsonlUtils.*;

/**
 * EVE Online ESI(OAuth2) 인증 및 토큰 관리를 담당하는 서비스 클래스.
 * SSO 콜백 처리, 토큰 갱신, 캐릭터 정보 연동 및 계정 병합 로직을 포함합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsiAuthService {

    private final EsiProperties esiProperties;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final EsiClient esiClient; // EsiClient 주입

    private static final String TOKEN_URL = "https://login.eveonline.com/v2/oauth/token";
    private static final String VERIFY_URL = "https://login.eveonline.com/oauth/verify";

    /**
     * EVE SSO 인증 후 콜백 리다이렉트를 처리합니다.
     * 코드를 토큰으로 교환하고, 캐릭터 정보를 조회하여 DB에 저장하거나 업데이트합니다.
     * 또한, 기존 사용자 그룹과의 연결 및 계정 병합(SeAT 스타일)을 수행합니다.
     *
     * @param code          EVE SSO로부터 받은 인증 코드
     * @param linkingUserId 현재 로그인된 사용자의 ID (캐릭터 추가 연결 시 사용)
     * @return 처리 완료된 캐릭터 엔티티
     */
    @Transactional
    public Character handleCallback(String code, Long linkingUserId) {
        // 1. 코드를 Access/Refresh Token으로 교환
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);

        JsonNode tokenResponse = esiClient.post(TOKEN_URL, body, basicAuth()).getBody().getFirst();

        log.info("ESI 인증 TOKEN: {}", tokenResponse);

        String accessToken = getText(tokenResponse, "access_token");
        String refreshToken = getText(tokenResponse, "refresh_token");
        Integer expiresIn = getInt(tokenResponse, "expires_in");
        if (expiresIn == null) expiresIn = 1200;
        LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(expiresIn);

        // 2. 토큰 검증 및 기본 캐릭터 정보(ID, 이름 등) 획득
        List<JsonNode> result = esiClient.get(VERIFY_URL, accessToken).getBody();
        JsonNode charInfo = result.isEmpty() ? null : result.getFirst();

        if (charInfo == null) throw new RuntimeException("ESI 토큰 검증 실패 (verifyToken failed)");

        Long characterId = getLong(charInfo, "CharacterID");
        String characterName = getText(charInfo, "CharacterName");

        if (characterId == null) throw new RuntimeException("토큰검증실패") ;

        Character pilot = characterRepository.findById(characterId)
                .map(existing -> {
                    existing.updateToken(accessToken, refreshToken, expiredAt);
                    return existing;
                })
                .orElse(Character.builder()
                        .characterId(characterId)
                        .characterName(characterName)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenExpiresAt(expiredAt)
                        .build());

        // 5. 사용자 그룹(User) 연결 및 병합 로직
        User currentUser = null;
        if (linkingUserId != null) {
            currentUser = userRepository.findById(linkingUserId)
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 User를 찾을 수 없습니다: " + linkingUserId));

            if (pilot.getUser() != null && !pilot.getUser().getId().equals(currentUser.getId())) {
                User oldUser = pilot.getUser();
                log.info("계정 병합 발생: User {} -> User {}", oldUser.getId(), currentUser.getId());

                List<Character> alts = characterRepository.findByUser(oldUser);
                for (Character alt : alts) {
                    alt.linkToUser(currentUser);
                    alt.demoteFromMain(); // 옮겨온 캐릭터들은 일단 메인 해제
                }
                characterRepository.saveAll(alts);
                userRepository.delete(oldUser); // 이전 빈 유저 그룹 삭제
            }
        }

        // 로그인하지 않은 상태에서 첫 캐릭터 연동 시 새 유저 그룹 생성
        if (currentUser == null) {
            if (pilot.getUser() != null) {
                currentUser = pilot.getUser();
            } else {
                currentUser = userRepository.save(User.builder().createdAt(LocalDateTime.now()).build());
                log.info("새 User 그룹 생성: id={}, character={}", currentUser.getId(), characterName);
            }
        }
        pilot.linkToUser(currentUser);

        // 현재 그룹에 메인 캐릭터가 하나도 없다면 이 캐릭터를 메인으로 설정
        if (!characterRepository.existsByUserAndMainTrue(currentUser)) {
            pilot.setAsMain();
        }

        return characterRepository.save(pilot);
    }

    /**
     * Refresh Token을 사용하여 Access Token을 갱신합니다.
     * 갱신된 토큰 정보는 즉시 DB에 저장됩니다.
     *
     * @param pilot 토큰을 갱신할 캐릭터 엔티티
     * @return 새로 발급된 Access Token
     */
    public String refreshAccessToken(Character pilot) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", pilot.getRefreshToken());

        // ESI 토큰 엔드포인트에 갱신 요청
        JsonNode response = esiClient.post(TOKEN_URL, body, basicAuth()).getBody().getFirst();

        String newReFreshToken = getText(response, "refresh_token");
        String newAccessToken = getText(response, "access_token");
        Integer expiresIn = getInt(response, "expires_in");
        if (expiresIn == null) expiresIn = 1200;
        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(expiresIn);


        // 엔티티 정보 업데이트 및 저장
        pilot.updateAccessToken(newReFreshToken, newAccessToken, newExpiresAt);
        characterRepository.save(pilot);

        log.info("토큰 갱신 완료: {}", pilot.getCharacterName());
        return newAccessToken;
    }

    /**
     * 캐릭터의 Access Token이 유효한지 확인하고, 만료된 경우 갱신하여 반환합니다.
     *
     * @param pilot 대상 캐릭터 엔티티
     * @return 유효한 Access Token
     */
    public String getValidToken(Character pilot) {
        if (pilot.isTokenExpired()) {
            log.info("토큰 만료 - 갱신 중: {}", pilot.getCharacterName());
            return refreshAccessToken(pilot);
        }
        return pilot.getAccessToken();
    }


    /**
     * 캐릭터의 공개 정보(소속 코퍼레이션, 얼라이언스 ID 및 초상화 URL)를 조회합니다.
     */
    private JsonNode fetchPublicInfo(Long characterId, String accessToken) {
        tools.jackson.databind.node.ObjectNode result =
                tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        try {
            // 소속 정보 조회
            List<JsonNode> infoList = esiClient.get("/characters/" + characterId + "/", accessToken).getBody();
            if (!infoList.isEmpty()) {
                JsonNode info = infoList.getFirst();
                Long corporationId = getLong(info, "corporation_id");
                Long allianceId    = getLong(info, "alliance_id");
                if (corporationId != null) result.put("corporation_id", corporationId);
                if (allianceId != null)    result.put("alliance_id", allianceId);
            }

            // 초상화 URL 조회
            List<JsonNode> portraitList = esiClient.get("/characters/" + characterId + "/portrait/", null).getBody();
            if (!portraitList.isEmpty()) {
                JsonNode portrait = portraitList.getFirst();
                String portraitUrl = getText(portrait, "px128x128");
                if (portraitUrl != null) {
                    result.put("portrait", portraitUrl);
                }
            }
        } catch (Exception e) {
            log.warn("캐릭터 공개정보 조회 실패 (characterId: {}): {}", characterId, e.getMessage());
        }
        return result;
    }

    /**
     * ESI 애플리케이션 인증을 위한 HTTP Basic Auth 헤더 값을 생성합니다.
     */
    private String basicAuth() {
        String credentials = esiProperties.getClientId() + ":" + esiProperties.getClientSecret();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
