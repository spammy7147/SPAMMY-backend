package spammy.eve.domain.character;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.client.EsiClient;
import spammy.eve.domain.auth.EsiAuthService;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final EsiClient esiClient;
    private final CharacterRepository characterRepository;
    private final EsiAuthService esiAuthService;
    private final EsiSyncService esiSyncService;

    @Transactional
    public void syncAll(Character character) {
        // 잔액 정보 업데이트
        esiSyncService.syncCharacterInfo(character);
        esiSyncService.syncMarketPrices();
        esiSyncService.syncWalletJournal(character, character.getAccessToken());
        esiSyncService.syncWalletTransactions(character, character.getAccessToken());
        esiSyncService.syncContracts(character, character.getAccessToken());
        esiSyncService.syncBlueprints(character, character.getAccessToken());
        esiSyncService.syncIndustryJobs(character, character.getAccessToken());
        esiSyncService.syncMarketOrders(character, character.getAccessToken());
        esiSyncService.syncAssets(character, character.getAccessToken());
        esiSyncService.syncLoyaltyPoints(character, character.getAccessToken());

        character.updateLastSyncedAt();



        log.info("ESI 동기화 완료 - {}", character.getCharacterName());
    }

    public void extendOmega(Map<String, String> body, Character character) {
        String dateStr = body.get("omegaExpiresAt");
        if (dateStr == null) {
            character.updateOmegaExpiresAt(null);
        } else {
            character.updateOmegaExpiresAt(LocalDate.parse(dateStr));
        }
        characterRepository.save(character);
    }
}
