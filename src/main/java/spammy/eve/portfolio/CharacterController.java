package spammy.eve.portfolio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import spammy.eve.portfolio.domain.Character;
import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.dto.*;
import spammy.eve.portfolio.response.*;
import spammy.eve.portfolio.service.CharacterService;
import spammy.eve.portfolio.service.UserService;

import java.util.Map;

import spammy.eve.portfolio.repository.UserSettingsRepository;
import spammy.eve.portfolio.repository.CharacterRepository;
import spammy.eve.portfolio.domain.UserSettings;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    private final CharacterService characterService;
    private final UserService userService;
    private final UserSettingsRepository userSettingsRepository;
    private final CharacterRepository characterRepository;

    /**
     * 유저의 설정을 조회합니다.
     */
    @GetMapping("/settings")
    public ResponseEntity<UserSettingsDto> getSettings(@AuthenticationPrincipal User user) {
        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> userSettingsRepository.save(UserSettings.builder()
                        .user(user)
                        .iskAbbreviation(true)
                        .timezone("UTC")
                        .build()));
        
        return ResponseEntity.ok(UserSettingsDto.builder()
                .userId(user.getId())
                .mainCharacterName(getMainCharacterName(user))
                .iskAbbreviation(settings.isIskAbbreviation())
                .timezone(settings.getTimezone())
                .build());
    }

    /**
     * 유저의 설정을 업데이트합니다.
     */
    @Transactional
    @PatchMapping("/settings")
    public ResponseEntity<UserSettingsDto> updateSettings(@AuthenticationPrincipal User user, @RequestBody UserSettingsDto dto) {
        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> UserSettings.builder().user(user).build());
        
        settings.updateIskAbbreviation(dto.isIskAbbreviation());
        if (dto.getTimezone() != null && !dto.getTimezone().isBlank()) {
            settings.updateTimezone(dto.getTimezone());
        }
        userSettingsRepository.save(settings);
        
        return ResponseEntity.ok(UserSettingsDto.builder()
                .userId(user.getId())
                .mainCharacterName(getMainCharacterName(user))
                .iskAbbreviation(settings.isIskAbbreviation())
                .timezone(settings.getTimezone())
                .build());
    }

    private String getMainCharacterName(User user) {
        return characterRepository.findByUserAndMainTrue(user)
                .map(Character::getCharacterName)
                .orElseGet(() -> characterRepository.findFirstByUser(user)
                        .map(Character::getCharacterName)
                        .orElse("Unknown"));
    }

    /**
     * 유저의 모든 캐릭터 요약 정보와 총 잔액을 반환합니다.
     */
    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(@AuthenticationPrincipal User user) {
        log.info("캐릭터 요약 정보 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getSummary(user));
    }

    /**
     * 유저의 모든 캐릭터 자산 정보를 반환합니다.
     */
    @GetMapping("/assets")
    public ResponseEntity<AssetResponse> getAssets(@AuthenticationPrincipal User user) {
        log.info("캐릭터 자산 정보 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getAssets(user));
    }

    /**
     * 유저의 모든 캐릭터 지갑 내역을 반환합니다.
     */
    @GetMapping("/journal")
    public ResponseEntity<JournalResponse> getJournal(@AuthenticationPrincipal User user) {
        log.info("캐릭터 지갑 내역 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getJournal(user));
    }

    /**
     * 유저의 모든 캐릭터 LP 정보를 반환합니다.
     */
    @GetMapping("/lp")
    public ResponseEntity<LpResponse> getLoyaltyPoints(@AuthenticationPrincipal User user) {
        log.info("캐릭터 LP 정보 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getLoyaltyPoints(user));
    }

    /**
     * 유저의 모든 캐릭터 미션 정보를 반환합니다.
     */
    @GetMapping("/missions")
    public ResponseEntity<MissionResponse> getMissions(@AuthenticationPrincipal User user) {
        log.info("캐릭터 미션 정보 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getMissions(user));
    }

    /**
     * 유저의 모든 캐릭터 평판 정보를 반환합니다.
     */
    @GetMapping("/standings")
    public ResponseEntity<StandingResponse> getStandings(@AuthenticationPrincipal User user) {
        log.info("캐릭터 평판 정보 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getStandings(user));
    }

    /**
     * 유저의 모든 캐릭터 마켓 거래 내역을 반환합니다.
     */
    @GetMapping("/transactions")
    public ResponseEntity<TransactionResponse> getTransactions(@AuthenticationPrincipal User user) {
        log.info("캐릭터 마켓 거래 내역 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getTransactions(user));
    }

    /**
     * 유저의 모든 캐릭터 마켓 오더 정보를 반환합니다.
     */
    @GetMapping("/orders")
    public ResponseEntity<OrderResponse> getOrders(@AuthenticationPrincipal User user) {
        log.info("캐릭터 마켓 오더 정보 요청 - user: {}", user);
        return ResponseEntity.ok(characterService.getOrders(user));
    }

    /**
     * 캐릭터의 오메가 만료 시간을 수동으로 업데이트합니다.
     */
    @Transactional
    @PatchMapping("/{characterId}/omega")
    public ResponseEntity<?> updateOmega(@PathVariable Long characterId,
                                        @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal User user) {
        Long userId = user.getId();
        Character character = userService.check(characterId, userId);
        characterService.setOmega(body, character);
        return ResponseEntity.ok().build();
    }
}
