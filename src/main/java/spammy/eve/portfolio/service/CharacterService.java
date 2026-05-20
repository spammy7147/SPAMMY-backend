package spammy.eve.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.portfolio.domain.Character;
import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.repository.*;
import spammy.eve.portfolio.response.*;
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
    private final WalletTransactionRepository walletTransactionRepository;
    private final MarketOrderRepository marketOrderRepository;
    private final EsiSyncService esiSyncService;

    public SummaryResponse getSummary(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                esiSyncService.syncCharacterInfo(character);
            } catch (Exception e) {
                log.error("Failed to sync character info for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return characterRepository.getSummary(user);
    }

    public AssetResponse getAssets(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                esiSyncService.syncAssets(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync assets for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return assetRepository.getAssets(user);
    }

    public JournalResponse getJournal(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                esiSyncService.syncWalletJournal(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync journal for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return walletJournalRepository.getJournal(user);
    }

    public LpResponse getLoyaltyPoints(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                esiSyncService.syncLoyaltyPoints(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync LP for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return loyaltyPointRepository.getLoyaltyPoints(user);
    }

    public MissionResponse getMissions(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                // 미션 정보는 주로 저널에서 추출하므로 저널 동기화 수행
                esiSyncService.syncWalletJournal(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync journal for missions {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return walletJournalRepository.getMissions(user);
    }

    public StandingResponse getStandings(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                esiSyncService.syncStandings(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync standings for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return standingRepository.getStandings(user);
    }

    public TransactionResponse getTransactions(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        log.info("getTransactions - userId: {}, characters found: {}", user.getId(), characters.size());
        for (Character character : characters) {
            log.info("캐릭터 : {}", character.getCharacterName());
            log.info("토큰 : {}", character.getAccessToken());
            try {
                esiSyncService.syncWalletTransactions(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync transactions for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        TransactionResponse response = walletTransactionRepository.getTransactions(user);
        log.info("getTransactions - userId: {}, total results in DB: {}", user.getId(), response.getEntries().size());
        return response;
    }

    public OrderResponse getOrders(User user) {
        List<Character> characters = characterRepository.findByUser(user);
        for (Character character : characters) {
            try {
                esiSyncService.syncMarketOrders(character);
                character.updateLastSyncedAt();
                characterRepository.save(character);
            } catch (Exception e) {
                log.error("Failed to sync orders for {}: {}", character.getCharacterName(), e.getMessage());
            }
        }
        return marketOrderRepository.getOrders(user);
    }

    @Transactional
    public void syncAll(Character character) {
        // 잔액 정보 업데이트
        esiSyncService.syncCharacterInfo(character);
        esiSyncService.syncMarketPrices();
        esiSyncService.syncWalletJournal(character);
        esiSyncService.syncWalletTransactions(character);
        esiSyncService.syncContracts(character);
        esiSyncService.syncBlueprints(character);
        esiSyncService.syncIndustryJobs(character);
        esiSyncService.syncMarketOrders(character);
        esiSyncService.syncAssets(character);
        esiSyncService.syncLoyaltyPoints(character);
        esiSyncService.syncStandings(character);
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
