package spammy.eve.character.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.character.domain.Character;
import spammy.eve.character.repository.CharacterRepository;
import spammy.eve.client.EsiClient;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final EsiClient esiClient;
    private final CharacterRepository characterRepository;
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
        esiSyncService.syncStandings(character, character.getAccessToken());
        character.updateLastSyncedAt();

        log.info("ESI 동기화 완료 - {}", character.getCharacterName());
    }

    public void setOmega(Map<String, String> body, Character character) {
        String dateStr = body.get("omegaExpiresAt");
        if (dateStr == null) {
            character.updateOmegaExpiresAt(null);
        } else {
            character.updateOmegaExpiresAt(LocalDateTime.parse(dateStr));
        }
        characterRepository.save(character);
    }
}
