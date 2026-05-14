package spammy.eve.character;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import spammy.eve.character.domain.Character;
import spammy.eve.character.dto.*;
import spammy.eve.character.service.CharacterService;
import spammy.eve.character.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    private final CharacterService characterService;
    private final UserService userService;

    /**
     * 유저의 모든 캐릭터 요약 정보와 총 잔액을 반환합니다.
     */
    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(@AuthenticationPrincipal Long userId) {
        log.info("캐릭터 요약 정보 요청 - userId: {}", userId);
        return ResponseEntity.ok(characterService.getSummary(userId));
    }

    /**
     * 유저의 모든 캐릭터 자산 정보를 반환합니다.
     */
    @GetMapping("/assets")
    public ResponseEntity<AssetResponse> getAssets(@AuthenticationPrincipal Long userId) {
        log.info("캐릭터 자산 정보 요청 - userId: {}", userId);
        return ResponseEntity.ok(characterService.getAssets(userId));
    }

    /**
     * 유저의 모든 캐릭터 지갑 내역을 반환합니다.
     */
    @GetMapping("/journal")
    public ResponseEntity<JournalResponse> getJournal(@AuthenticationPrincipal Long userId) {
        log.info("캐릭터 지갑 내역 요청 - userId: {}", userId);
        return ResponseEntity.ok(characterService.getJournal(userId));
    }

    /**
     * 유저의 모든 캐릭터 LP 정보를 반환합니다.
     */
    @GetMapping("/lp")
    public ResponseEntity<LpResponse> getLoyaltyPoints(@AuthenticationPrincipal Long userId) {
        log.info("캐릭터 LP 정보 요청 - userId: {}", userId);
        return ResponseEntity.ok(characterService.getLoyaltyPoints(userId));
    }

    /**
     * 유저의 모든 캐릭터 미션 정보를 반환합니다.
     */
    @GetMapping("/missions")
    public ResponseEntity<MissionResponse> getMissions(@AuthenticationPrincipal Long userId) {
        log.info("캐릭터 미션 정보 요청 - userId: {}", userId);
        return ResponseEntity.ok(characterService.getMissions(userId));
    }

    /**
     * 유저의 모든 캐릭터 평판 정보를 반환합니다.
     */
    @GetMapping("/standings")
    public ResponseEntity<StandingResponse> getStandings(@AuthenticationPrincipal Long userId) {
        log.info("캐릭터 평판 정보 요청 - userId: {}", userId);
        return ResponseEntity.ok(characterService.getStandings(userId));
    }

    /**
     * 캐릭터의 오메가 만료 시간을 수동으로 업데이트합니다.
     */
    @Transactional
    @PatchMapping("/{characterId}/omega")
    public ResponseEntity<?> updateOmega(@PathVariable Long characterId,
                                        @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal Long userId) {

        Character character = userService.check(characterId, userId);
        characterService.setOmega(body, character);
        return ResponseEntity.ok().build();
    }
}
