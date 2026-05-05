package spammy.eve.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import spammy.eve.character.domain.Character;
import spammy.eve.character.repository.CharacterRepository;
import spammy.eve.user.User;
import spammy.eve.user.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * EVE Online ESI(OAuth2)를 통한 사용자 인증 및 캐릭터 연동을 담당하는 서비스입니다.
 * Spring Security의 DefaultOAuth2UserService를 상속받아 EVE SSO의 verify 정보를 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EveOAuth2UserService extends DefaultOAuth2UserService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 설정을 통해 EVE SSO로부터 유저 정보(verify 정보)를 가져옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. EVE SSO 응답값에서 캐릭터 ID와 이름을 추출합니다.
        Long characterId = Long.valueOf(attributes.get("CharacterID").toString());
        String characterName = (String) attributes.get("CharacterName");

        // 3. 발급받은 Access Token과 Refresh Token 정보를 확보합니다.
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String refreshToken = (String) userRequest.getAdditionalParameters().get("refresh_token");

        assert userRequest.getAccessToken().getExpiresAt() != null;
        LocalDateTime expiredAt = LocalDateTime.ofInstant(userRequest.getAccessToken().getExpiresAt(), ZoneId.systemDefault());

        log.info("EVE SSO 로그인 시도: characterId={}, characterName={}", characterId, characterName);

        // 4. 기존에 등록된 캐릭터인지 확인하고 토큰 정보를 갱신하거나 새로 생성합니다.
        Character pilot = characterRepository.findById(characterId)
                .map(existing -> {
                    String rt = refreshToken != null ? refreshToken : existing.getRefreshToken();
                    existing.updateToken(accessToken, rt, expiredAt);
                    return existing;
                })
                .orElse(Character.builder()
                        .characterId(characterId)
                        .characterName(characterName)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken != null ? refreshToken : "")
                        .tokenExpiresAt(expiredAt)
                        .build());

        // 5. '캐릭터 연동(Link)' 모드인지 확인합니다. (쿠키에 user_id가 있는 경우)
        Long linkingUserId = getLinkingUserIdFromCookie();
        User currentUser = null;

        if (linkingUserId != null) {
            // 현재 로그인된 유저가 새 캐릭터를 추가하려는 경우
            currentUser = userRepository.findById(linkingUserId).orElse(null);

            if (currentUser != null && pilot.getUser() != null && !pilot.getUser().getId().equals(currentUser.getId())) {
                // [계정 병합]: 추가하려는 캐릭터가 이미 다른 유저 그룹에 속해있다면 그룹을 합칩니다 (SeAT 스타일).
                User oldUser = pilot.getUser();
                log.info("계정 병합 발생: User {} (이전 그룹) -> User {} (현재 그룹)", oldUser.getId(), currentUser.getId());

                List<Character> alts = characterRepository.findByUser(oldUser);
                for (Character alt : alts) {
                    alt.linkToUser(currentUser);
                    alt.demoteFromMain();
                }
                characterRepository.saveAll(alts);
                userRepository.delete(oldUser);
            }
        }

        // 6. 연결된 유저 그룹이 없다면 새 유저 그룹을 생성하거나 기존 그룹을 할당합니다.
        if (currentUser == null) {
            if (pilot.getUser() != null) {
                currentUser = pilot.getUser();
            } else {
                currentUser = userRepository.save(User.builder().createdAt(LocalDateTime.now()).build());
                log.info("새로운 유저 그룹 생성: id={}, 대표캐릭터={}", currentUser.getId(), characterName);
            }
        }
        pilot.linkToUser(currentUser);

        // 7. 유저 그룹에 메인 캐릭터가 없다면 현재 캐릭터를 메인으로 설정합니다.
        if (!characterRepository.existsByUserAndMainTrue(currentUser)) {
            pilot.setAsMain();
        }

        characterRepository.save(pilot);

        // 8. 우리 서비스의 내부 userId를 포함한 CustomOAuth2User를 반환합니다.
        return new CustomOAuth2User(currentUser.getId(), attributes, oAuth2User.getAuthorities(), characterName);
    }

    /**
     * 캐릭터 연동(Link) 요청 시 쿠키에 저장된 user_id를 읽어옵니다.
     */
    private Long getLinkingUserIdFromCookie() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return null;
        HttpServletRequest request = attr.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> "user_id".equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> !v.isEmpty())
                .map(Long::parseLong)
                .findFirst()
                .orElse(null);
    }
}
