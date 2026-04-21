package spammy.eve.domain.character;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.user.User;
import spammy.eve.domain.character.CharacterRepository;
import spammy.eve.domain.user.UserRepository;
import spammy.eve.domain.user.UserService;
import spammy.eve.global.auth.JwtService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    private final CharaterService charaterService;
    private final JwtService jwtService;
    private final UserService userService;

    /**
     * 캐릭터의 오메가 만료 시간을 수동으로 업데이트합니다.
     */
    @Transactional
    @PatchMapping("/{characterId}/omega")
    public ResponseEntity<?> updateOmega(@PathVariable Long characterId, 
                                        @RequestBody Map<String, String> body,
                                         @CookieValue(name = "auth_token", required = false) String authToken) {

        Character character = userService.check(characterId, jwtService.getUserId(authToken));

        charaterService.extendOmega(body, character);
        return ResponseEntity.ok().build();
    }



    @Transactional
    @GetMapping("/{characterId}/")
    public ResponseEntity<?> getCharacterInfo(@PathVariable Long characterId,
                                              @CookieValue(name = "auth_token", required = false) String authToken) {

        Character character = userService.check(characterId, jwtService.getUserId(authToken));



        https://esi.evetech.net/characters/{character_id}

        return ResponseEntity.ok().build();
    }
}
