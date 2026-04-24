package spammy.eve.domain.character;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import spammy.eve.domain.user.UserService;
import spammy.eve.global.auth.JwtService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    private final CharacterService characterService;
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

        characterService.extendOmega(body, character);
        return ResponseEntity.ok().build();
    }



    @Transactional
    @GetMapping("/{characterId}/")
    public ResponseEntity<?> getCharacterInfo(@PathVariable Long characterId,
                                              @CookieValue(name = "auth_token", required = false) String authToken) {

        Character character = userService.check(characterId, jwtService.getUserId(authToken));
        characterService.syncAll(character);


        return ResponseEntity.ok().build();
    }
}
