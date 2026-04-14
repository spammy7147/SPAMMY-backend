package spammy.eve.domain.character;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.client.EsiClient;
import spammy.eve.domain.auth.EsiAuthService;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static spammy.eve.global.utils.JsonlUtils.getDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiService {

    private final EsiClient esiClient;
    private final EsiAuthService esiAuthService;
    private final EsiSyncService esiSyncService;
    private final CharacterRepository characterRepository;

    // ISK 잔액
    public double getWalletBalance(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("캐릭터 없음"));
        String token = esiAuthService.getValidToken(character);
        List<JsonNode> jsonNodes = esiClient.get(
                "/characters/" + characterId + "/wallet/",
                token
        ).getBody();
        log.info("jsonNode = {}", jsonNodes);
        Double balance = jsonNodes.isEmpty() ? 0.0 : getDouble(jsonNodes.getFirst(), null);
        return balance == null ? 0.0 : balance;
    }

    // 지갑 거래 내역
    public JsonNode getWalletTransactions(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("캐릭터 없음"));
        String token = esiAuthService.getValidToken(character);
        List<JsonNode> jsonNodes = esiClient.get(
                "/characters/" + characterId + "/wallet/transactions/",
                token
        ).getBody();
        return jsonNodes.isEmpty() ? null : jsonNodes.getFirst();
    }

    // 자산 목록
    public JsonNode getAssets(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("캐릭터 없음"));
        String token = esiAuthService.getValidToken(character);
        List<JsonNode> jsonNodes = esiClient.get(
                "/characters/" + characterId + "/assets/",
                token
        ).getBody();
        return jsonNodes.isEmpty() ? null : jsonNodes.getFirst();
    }

    // 지갑 저널 (모든 ISK 입출금)
    public JsonNode getWalletJournal(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("캐릭터 없음"));
        String token = esiAuthService.getValidToken(character);
        List<JsonNode> jsonNodes = esiClient.get(
                "/characters/" + characterId + "/wallet/journal/",
                token
        ).getBody();
        return jsonNodes.isEmpty() ? null : jsonNodes.getFirst();
    }


    @Transactional
    public void syncAll(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("캐릭터 없음"));
        String token = esiAuthService.getValidToken(character);

        log.info("ESI 동기화 시작 - {}", character.getCharacterName());
        
        // 잔액 정보 업데이트
        double balance = getWalletBalance(characterId);
        character.updateBalance(balance);

        esiSyncService.syncCharacterInfo(character);
        esiSyncService.syncMarketPrices();
        esiSyncService.syncWalletJournal(character, token);
        esiSyncService.syncWalletTransactions(character, token);
        esiSyncService.syncContracts(character, token);
        esiSyncService.syncBlueprints(character, token);
        esiSyncService.syncIndustryJobs(character, token);
        esiSyncService.syncMarketOrders(character, token);
        esiSyncService.syncAssets(character, token);
        esiSyncService.syncLoyaltyPoints(character, token);
        character.updateLastSyncedAt();
        characterRepository.save(character);
        log.info("ESI 동기화 완료 - {}", character.getCharacterName());
    }
}
