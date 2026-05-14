package spammy.eve.character.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.character.domain.Character;
import spammy.eve.character.dto.*;
import spammy.eve.character.repository.*;
import spammy.eve.client.EsiClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final EsiClient esiClient;
    private final CharacterRepository characterRepository;
    private final AssetRepository assetRepository;
    private final WalletJournalRepository walletJournalRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final StandingRepository standingRepository;
    private final EsiSyncService esiSyncService;

    public SummaryResponse getSummary(Long userId) {
        List<Character> characters = characterRepository.findByUser_Id(userId);
        for (Character character : characters) {
            try {
                esiSyncService.syncCharacterInfo(character, character.getAccessToken());
            } catch (Exception e) {
                log.error("Failed to sync character info for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return characterRepository.getSummary(userId);
    }

    public AssetResponse getAssets(Long userId) {
        List<Character> characters = characterRepository.findByUser_Id(userId);
        for (Character character : characters) {
            try {
                esiSyncService.syncAssets(character, character.getAccessToken());
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync assets for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return assetRepository.getAssets(userId);
    }

    public JournalResponse getJournal(Long userId) {
        List<Character> characters = characterRepository.findByUser_Id(userId);
        for (Character character : characters) {
            try {
                esiSyncService.syncWalletJournal(character, character.getAccessToken());
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync journal for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return walletJournalRepository.getJournal(userId);
    }

    public LpResponse getLoyaltyPoints(Long userId) {
        List<Character> characters = characterRepository.findByUser_Id(userId);
        for (Character character : characters) {
            try {
                esiSyncService.syncLoyaltyPoints(character, character.getAccessToken());
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync LP for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return loyaltyPointRepository.getLoyaltyPoints(userId);
    }

    public MissionResponse getMissions(Long userId) {
        List<Character> characters = characterRepository.findByUser_Id(userId);
        for (Character character : characters) {
            try {
                // 미션 정보는 주로 저널에서 추출하므로 저널 동기화 수행
                esiSyncService.syncWalletJournal(character, character.getAccessToken());
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync journal for missions {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return walletJournalRepository.getMissions(userId);
    }

    public StandingResponse getStandings(Long userId) {
        List<Character> characters = characterRepository.findByUser_Id(userId);
        for (Character character : characters) {
            try {
                esiSyncService.syncStandings(character, character.getAccessToken());
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync standings for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return standingRepository.getStandings(userId);
    }

    @Transactional
    public void syncAll(Character character) {
        // 잔액 정보 업데이트
        esiSyncService.syncCharacterInfo(character, character.getAccessToken());
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
