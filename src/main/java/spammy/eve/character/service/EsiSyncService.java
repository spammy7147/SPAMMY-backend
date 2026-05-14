package spammy.eve.character.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.character.domain.*;
import spammy.eve.character.domain.Character;
import spammy.eve.character.repository.*;
import spammy.eve.client.EsiClient;
import spammy.eve.client.EsiResponse;
import spammy.eve.market.MarketPrice;
import spammy.eve.market.MarketPriceRepository;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static spammy.eve.global.utils.JsonlUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiSyncService {

    private final EsiClient esiClient;
    private final WalletJournalRepository walletJournalRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CharacterContractRepository contractRepository;
    private final ContractItemRepository contractItemRepository;
    private final CharacterBlueprintRepository characterBlueprintRepository;
    private final IndustryJobRepository industryJobRepository;
    private final MarketOrderRepository marketOrderRepository;
    private final AssetRepository assetRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final StandingRepository standingRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final CharacterRepository characterRepository;

    // ── 캐릭터 공개 정보 (corp, alliance, portrait) ──────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncCharacterInfo(Character character, String token) {
        log.info("캐릭터 정보 동기화 중...");
        EsiResponse characterResponse = esiClient.get("/characters/" + character.getCharacterId() + "/", null, null);
        EsiResponse portraitResponse = esiClient.get("/characters/" + character.getCharacterId() + "/portrait/", null, null);

        if(characterResponse.isModified()) {
            Long corporationId = getLong(characterResponse.getBody(), "corporation_id");
            Long allianceId    = getLong(characterResponse.getBody(), "alliance_id");
            character.updateInfo(corporationId,allianceId);

            EsiResponse alliancesResponse = esiClient.get("/alliances/" + allianceId, null, null);
            EsiResponse corporationResponse= esiClient.get("/corporations/" + corporationId, null, null);
            String allianceName = null;
            String corporationName = null;
            if(corporationResponse.isModified()) corporationName = getString(corporationResponse.getBody(), "name");
            if(alliancesResponse.isModified()) allianceName = getString(alliancesResponse.getBody(), "name");

            character.updateCorpAndAllianceName(corporationName, allianceName);
        }

        if(portraitResponse.isModified()) {
            String portraitUrl = getString(portraitResponse.getBody(), "px128x128");
            character.updatePortrait(portraitUrl);
        }

        if (token != null) {
            EsiResponse walletResponse = esiClient.get("/characters/" + character.getCharacterId() + "/wallet/", token, null);
            if (walletResponse.isModified()) {
                character.updateBalance(walletResponse.getBody().asDouble());
            }
        }
        
        character.updateLastSyncedAt();
        characterRepository.save(character);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncWalletJournal(Character character, String token) {
        log.info("WalletJournal 동기화 중...");
        Long lastJournalId = walletJournalRepository.findTopByCharacterCharacterIdOrderByJournalIdDesc(character.getCharacterId())
                .map(WalletJournal::getJournalId)
                .orElse(0L);

        EsiResponse walletJournalResponse = esiClient.get("/characters/" + character.getCharacterId() + "/wallet/journal/", token, null);
        if (walletJournalResponse.isModified()) {
            List<WalletJournal> batch = new ArrayList<>();
            for (JsonNode node : walletJournalResponse.getBody()) {
                Long journalId = getLong(node, "id");
                if (journalId <= lastJournalId) break;

                batch.add(WalletJournal.builder()
                        .journalId(journalId)
                        .character(character)
                        .date(OffsetDateTime.parse(getString(node, "date")))
                        .refType(getString(node, "ref_type"))
                        .amount(getDouble(node, "amount"))
                        .balance(getDouble(node, "balance"))
                        .description(getString(node, "description"))
                        .firstPartyId(getLong(node, "first_party_id"))
                        .secondPartyId(getLong(node, "second_party_id"))
                        .tax(getDouble(node, "tax"))
                        .taxReceiverId(getLong(node, "tax_receiver_id"))
                        .contextId(getLong(node, "context_id"))
                        .contextIdType(getString(node, "context_id_type"))
                        .build());
            }
            walletJournalRepository.saveAll(batch);
            log.info("WalletJournal 동기화 완료 ({}건 추가)", batch.size());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncWalletTransactions(Character character, String token) {
        log.info("WalletTransaction 동기화 중...");
        Long lastTransactionId = walletTransactionRepository.findTopByCharacterCharacterIdOrderByTransactionIdDesc(character.getCharacterId())
                .map(WalletTransaction::getTransactionId)
                .orElse(0L);

        EsiResponse walletTransactionResponse = esiClient.get("/characters/" + character.getCharacterId() + "/wallet/transactions/", token, null);
        if (!walletTransactionResponse.isModified()) return;

        List<WalletTransaction> batch = new ArrayList<>();
        for (JsonNode node : walletTransactionResponse.getBody()) {
            Long transactionId = getLong(node, "transaction_id");
            if (transactionId <= lastTransactionId) break;

            batch.add(WalletTransaction.builder()
                    .transactionId(transactionId)
                    .character(character)
                    .date(Instant.parse(getString(node, "date")))
                    .typeId(getLong(node, "type_id"))
                    .quantity(getInt(node, "quantity"))
                    .unitPrice(getDouble(node, "unit_price"))
                    .isBuy(getBoolean(node, "is_buy"))
                    .isPersonal(getBoolean(node, "is_personal"))
                    .locationId(getLong(node, "location_id"))
                    .clientId(getLong(node, "client_id"))
                    .journalRefId(getLong(node, "journal_ref_id"))
                    .build());
        }
        walletTransactionRepository.saveAll(batch);
        log.info("WalletTransaction 동기화 완료 ({}건 추가)", batch.size());
    }

    // ── 컨트랙트 ─────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncContracts(spammy.eve.character.domain.Character character, String token) {
        log.info("Contract 동기화 중...");

        EsiResponse contractResponse = esiClient.get("/characters/" + character.getCharacterId() + "/contracts/", token, null);
        if(!contractResponse.isModified()) return;

        List<CharacterContract> batch = new ArrayList<>();
        for (JsonNode node : contractResponse.getBody()) {
            Long contractId = getLong(node, "contract_id");
            if(contractId == null) continue;

            contractRepository.findById(contractId).ifPresentOrElse(
                    existing -> {
                        String newStatus = getString(node, "status");
                        Instant completedAt = parseInstant(node, "date_completed");
                        if (!existing.getStatus().equals(newStatus)) {
                            existing.updateStatus(newStatus, completedAt);
                            batch.add(existing);
                        }
                    },
                    () -> {
                        CharacterContract newContract = CharacterContract.builder()
                                .contractId(contractId)
                                .character(character)
                                .contractType(getString(node, "type"))
                                .status(getString(node, "status"))
                                .title(getString(node, "title"))
                                .issuerId(getLong(node, "issuer_id"))
                                .assigneeId(getLong(node, "assignee_id"))
                                .acceptorId(getLong(node, "acceptor_id"))
                                .price(getDouble(node, "price"))
                                .reward(getDouble(node, "reward"))
                                .collateral(getDouble(node, "collateral"))
                                .volume(getDouble(node, "volume"))
                                .dateIssued(parseInstant(node, "date_issued"))
                                .dateExpired(parseInstant(node, "date_expired"))
                                .dateCompleted(parseInstant(node, "date_completed"))
                                .startLocationId(getLong(node, "start_location_id"))
                                .endLocationId(getLong(node, "end_location_id"))
                                .forCorporation(getBoolean(node, "for_corporation"))
                                .build();
                        batch.add(newContract);
                        syncContractItems(character, token, newContract);
                    });
        }
        contractRepository.saveAll(batch);
        log.info("Contract 동기화 완료 ({}건 처리)", batch.size());
    }

    private void syncContractItems(spammy.eve.character.domain.Character character, String token, CharacterContract contract) {
        EsiResponse contractItemResponse = esiClient.get(
                "/characters/"
                        + character.getCharacterId()
                        + "/contracts/"
                        + contract.getContractId()
                        + "/items/",
                token,
                null);

        if(!contractItemResponse.isModified()) return;

        List<ContractItem> items = new ArrayList<>();
        for (JsonNode node : contractItemResponse.getBody()) {
            items.add(ContractItem.builder()
                    .recordId(getLong(node, "record_id"))
                    .contract(contract)
                    .typeId(getLong(node, "type_id"))
                    .quantity(getInt(node, "quantity"))
                    .isIncluded(getBoolean(node, "is_included"))
                    .isSingleton(getBoolean(node, "is_singleton"))
                    .rawQuantity(getInt(node, "raw_quantity"))
                    .build());
        }
        contractItemRepository.saveAll(items);
    }

    // ── 블루프린트 ───────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncBlueprints(spammy.eve.character.domain.Character character, String token) {
        log.info("Blueprint 동기화 중...");
        EsiResponse blueprintsResponse = esiClient.get("/characters/"
                                                            + character.getCharacterId()
                                                            + "/blueprints/",
                                                            token,
                                                        null);

        if(!blueprintsResponse.isModified()) return;
        List<CharacterBlueprint> batch = new ArrayList<>();
        for (JsonNode node : blueprintsResponse.getBody()) {
            Long itemId = getLong(node, "item_id");
            if (itemId == null) continue;
            characterBlueprintRepository.findById(itemId).ifPresentOrElse(
                    existing -> existing.update(
                            getInt(node, "material_efficiency"),
                            getInt(node, "time_efficiency"),
                            getInt(node, "runs"),
                            getLong(node, "location_id")),
                    () -> batch.add(CharacterBlueprint.builder()
                            .itemId(itemId)
                            .character(character)
                            .typeId(getLong(node, "type_id"))
                            .locationId(getLong(node, "location_id"))
                            .locationFlag(getString(node, "location_flag"))
                            .quantity(getInt(node, "quantity"))
                            .timeEfficiency(getInt(node, "time_efficiency"))
                            .materialEfficiency(getInt(node, "material_efficiency"))
                            .runs(getInt(node, "runs"))
                            .build())
            );
        }
        characterBlueprintRepository.saveAll(batch);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncIndustryJobs(Character character, String token) {
        log.info("IndustryJob 동기화 중...");
        EsiResponse industryJobResponse = esiClient.get("/characters/"
                                                                + character.getCharacterId()
                                                                + "/industry/jobs/?include_completed=true",
                                                            token,
                                                        null);

        if(!industryJobResponse.isModified()) return;

        List<IndustryJob> batch = new ArrayList<>();

        for (JsonNode node : industryJobResponse.getBody()) {
                Long jobId = getLong(node, "job_id");
                if (jobId == null) continue;

                industryJobRepository.findById(jobId).ifPresentOrElse(
                        existing -> existing.updateStatus(
                                getString(node, "status"),
                                parseInstant(node, "completed_date"),
                                getInt(node, "successful_runs")),
                        () -> batch.add(spammy.eve.character.domain.IndustryJob.builder()
                                .jobId(jobId)
                                .character(character)
                                .activityId(getInt(node, "activity_id"))
                                .blueprintTypeId(getLong(node, "blueprint_type_id"))
                                .blueprintLocationId(getLong(node, "blueprint_location_id"))
                                .outputLocationId(getLong(node, "output_location_id"))
                                .facilityId(getLong(node, "facility_id"))
                                .stationId(getLong(node, "station_id"))
                                .runs(getInt(node, "runs"))
                                .cost(getDouble(node, "cost"))
                                .licensedRuns(getInt(node, "licensed_runs"))
                                .probability(getDouble(node, "probability"))
                                .productTypeId(getLong(node, "product_type_id"))
                                .status(getString(node, "status"))
                                .duration(getInt(node, "duration"))
                                .startDate(parseInstant(node, "start_date"))
                                .endDate(parseInstant(node, "end_date"))
                                .pauseDate(parseInstant(node, "pause_date"))
                                .completedDate(parseInstant(node, "completed_date"))
                                .completedCharacterId(getLong(node, "completed_character_id"))
                                .successfulRuns(getInt(node, "successful_runs"))
                                .build())
                );
            }
        industryJobRepository.saveAll(batch);
        log.info("IndustryJob 동기화 완료 ({}건 추가)", batch.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncMarketOrders(Character character, String token) {
        log.info("MarketOrder 동기화 중...");

        // 1. 활성 오더 동기화
        EsiResponse activeOrdersResponse = esiClient.get("/characters/" + character.getCharacterId() + "/orders/", token, null);
        List<MarketOrder> activeOrders = new ArrayList<>();
        if (activeOrdersResponse.isModified()) {
            for (JsonNode node : activeOrdersResponse.getBody()) {
                activeOrders.add(MarketOrder.builder()
                        .orderId(getLong(node, "order_id"))
                        .character(character)
                        .typeId(getLong(node, "type_id"))
                        .locationId(getLong(node, "location_id"))
                        .regionId(getLong(node, "region_id"))
                        .price(getDouble(node, "price"))
                        .volumeTotal(getInt(node, "volume_total"))
                        .volumeRemain(getInt(node, "volume_remain"))
                        .isBuyOrder(getBoolean(node, "is_buy_order"))
                        .duration(getInt(node, "duration"))
                        .minVolume(getInt(node, "min_volume"))
                        .range(getString(node, "range"))
                        .issued(parseInstant(node, "issued"))
                        .escrow(getDouble(node, "escrow"))
                        .isCorporation(getBoolean(node, "is_corporation"))
                        .state("active")
                        .build());
            }
            marketOrderRepository.saveAll(activeOrders);
        }

        // 2. 히스토리 오더 동기화 (최근 90일)
        EsiResponse historicalOrdersResponse = esiClient.get("/characters/" + character.getCharacterId() + "/orders/history/", token, null);
        if (historicalOrdersResponse.isModified()) {
            List<MarketOrder> historicalOrders = new ArrayList<>();
            for (JsonNode node : historicalOrdersResponse.getBody()) {
                Long orderId = getLong(node, "order_id");
                // 이미 DB에 있는 오더인지 확인 (히스토리에서 상태 업데이트 필요할 수 있음)
                historicalOrders.add(MarketOrder.builder()
                        .orderId(orderId)
                        .character(character)
                        .typeId(getLong(node, "type_id"))
                        .locationId(getLong(node, "location_id"))
                        .regionId(getLong(node, "region_id"))
                        .price(getDouble(node, "price"))
                        .volumeTotal(getInt(node, "volume_total"))
                        .volumeRemain(getInt(node, "volume_remain"))
                        .isBuyOrder(getBoolean(node, "is_buy_order"))
                        .duration(getInt(node, "duration"))
                        .minVolume(getInt(node, "min_volume"))
                        .range(getString(node, "range"))
                        .issued(parseInstant(node, "issued"))
                        .escrow(getDouble(node, "escrow"))
                        .isCorporation(getBoolean(node, "is_corporation"))
                        .state(getString(node, "state"))
                        .completedAt(parseInstant(node, "completed_at"))
                        .build());
            }
            marketOrderRepository.saveAll(historicalOrders);
        }
        log.info("MarketOrder 동기화 완료");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncAssets(Character character, String token) {
        log.info("Asset 동기화 중...");
        EsiResponse assetsResponse = esiClient.get("/characters/" + character.getCharacterId() + "/assets/", token, null);
        if(!assetsResponse.isModified()) return;

        List<Asset> batch = new ArrayList<>();
        List<Long> currentItemIds = new ArrayList<>();
        for (JsonNode node : assetsResponse.getBody()) {
            Long itemId = getLong(node, "item_id");
            currentItemIds.add(itemId);
            batch.add(Asset.builder()
                    .itemId(itemId)
                    .character(character)
                    .typeId(getLong(node, "type_id"))
                    .locationId(getLong(node, "location_id"))
                    .locationType(getString(node, "location_type"))
                    .locationFlag(getString(node, "location_flag"))
                    .quantity(getInt(node, "quantity"))
                    .isSingleton(getBoolean(node, "is_singleton"))
                    .isBlueprintCopy(getBoolean(node, "is_blueprint_copy"))
                    .build());
        }
        assetRepository.saveAll(batch);
        if (!currentItemIds.isEmpty()) {
            assetRepository.deleteByCharacterCharacterIdAndItemIdNotIn(character.getCharacterId(), currentItemIds);
        } else {
            assetRepository.deleteByCharacterCharacterId(character.getCharacterId());
        }
        log.info("Asset 동기화 완료 ({}건)", batch.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncLoyaltyPoints(Character character, String token) {
        log.info("LoyaltyPoint 동기화 중...");
        EsiResponse loyaltyPointsResponse = esiClient.get("/characters/" + character.getCharacterId() + "/loyalty/points/", token, null);
        if(!loyaltyPointsResponse.isModified()) return;

        List<LoyaltyPoint> batch = new ArrayList<>();
        for (JsonNode node : loyaltyPointsResponse.getBody()) {
            Long corporationId = getLong(node, "corporation_id");
            Integer points = getInt(node, "loyalty_points");
            if (corporationId == null || points == null) continue;

            loyaltyPointRepository
                    .findByCharacterCharacterIdAndCorporationId(character.getCharacterId(), corporationId)
                    .ifPresentOrElse(
                            existing -> existing.update(points),
                            () -> batch.add(LoyaltyPoint.builder()
                                    .character(character)
                                    .corporationId(corporationId)
                                    .loyaltyPoints(points)
                                    .build())
                    );

        }
        loyaltyPointRepository.saveAll(batch);
        log.info("LoyaltyPoint 동기화 완료");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncStandings(Character character, String token) {
        log.info("Standing 동기화 중...");
        EsiResponse standingsResponse = esiClient.get("/characters/"
                                                                + character.getCharacterId()
                                                                + "/standings/",
                                                            token,
                                                            null);
        if(!standingsResponse.isModified()) return;

        List<Standing> batch = new ArrayList<>();
        for (JsonNode node : standingsResponse.getBody()) {
            Long fromId = getLong(node, "from_id");
            String fromType = getString(node, "from_type");
            Double value = getDouble(node, "standing");
            if (fromId == null || fromType == null || value == null) continue;

            standingRepository
                    .findByCharacterCharacterIdAndFromIdAndFromType(character.getCharacterId(), fromId, fromType)
                    .ifPresentOrElse(
                            existing -> existing.update(value),
                            () -> batch.add(Standing.builder()
                                    .character(character)
                                    .fromId(fromId)
                                    .fromType(fromType)
                                    .standingValue(value)
                                    .build())
                    );
        }
        standingRepository.saveAll(batch);
        log.info("Standing 동기화 완료");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncMarketPrices() {
        log.info("MarketPrice 동기화 중...");
        List<MarketPrice> batch = new ArrayList<>();

        for (JsonNode page : esiClient.get("/markets/prices/", null, null).getBody()) {
            if (!page.isArray()) continue;
            for (JsonNode node : page) {
                Long typeId = getLong(node, "type_id");
                if (typeId == null) continue;
                batch.add(MarketPrice.builder()
                        .typeId(typeId)
                        .averagePrice(getDouble(node, "average_price"))
                        .adjustedPrice(getDouble(node, "adjusted_price"))
                        .updatedAt(Instant.now())
                        .build());
            }
        }

        marketPriceRepository.saveAll(batch);
        log.info("MarketPrice 동기화 완료 ({}건)", batch.size());
    }

    // ── 유틸 ─────────────────────────────────────────────────────────

    private Instant parseInstant(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) return null;
        try {
            return Instant.parse(value.asString());
        } catch (Exception e) {
            return null;
        }
    }
}
