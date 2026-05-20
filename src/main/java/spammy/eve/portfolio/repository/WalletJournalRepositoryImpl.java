package spammy.eve.portfolio.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.portfolio.domain.QCharacter;
import spammy.eve.portfolio.domain.QWalletJournal;
import spammy.eve.portfolio.dto.JournalQueryDto;
import spammy.eve.portfolio.response.JournalResponse;
import spammy.eve.portfolio.response.MissionResponse;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import spammy.eve.portfolio.domain.LoyaltyPointHistory;
import spammy.eve.portfolio.domain.User;

@RequiredArgsConstructor
public class WalletJournalRepositoryImpl implements WalletJournalRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final LoyaltyPointHistoryRepository loyaltyPointHistoryRepository;

    @Override
    public JournalResponse getJournal(User user) {
        QWalletJournal journal = QWalletJournal.walletJournal;
        QCharacter character = QCharacter.character;

        List<JournalQueryDto> results = queryFactory
                .select(Projections.constructor(JournalQueryDto.class,
                        journal.date,
                        journal.refType,
                        journal.amount,
                        journal.balance,
                        journal.description,
                        journal.firstPartyId,
                        character.characterName))
                .from(journal)
                .join(journal.character, character)
                .where(character.user.id.eq(user.getId()))
                .orderBy(journal.date.desc())
                .limit(1000) // 최근 1000건만
                .fetch();


        List<JournalResponse.JournalEntry> entries = new ArrayList<>();
        Map<String, JournalResponse.TypeSummary> summaryMap = new HashMap<>();

        for (JournalQueryDto dto : results) {
            String charName = dto.getCharacterName();
            String mappedType = mapRefType(dto.getRefType());

            entries.add(JournalResponse.JournalEntry.builder()
                    .date(dto.getDate())
                    .charName(charName)
                    .type(mappedType)
                    .desc(dto.getDescription())
                    .amount(dto.getAmount())
                    .balance(dto.getBalance())
                    .build());

            JournalResponse.TypeSummary summary = summaryMap.computeIfAbsent(mappedType, 
                k -> JournalResponse.TypeSummary.builder().count(0).total(0.0).build());
            
            // summaryMap의 value는 immutable(Builder로 생성됨)이므로 새로 생성해야 함
            summaryMap.put(mappedType, JournalResponse.TypeSummary.builder()
                    .count(summary.getCount() + 1)
                    .total(summary.getTotal() + (dto.getAmount() != null ? dto.getAmount() : 0.0))
                    .build());
        }

        return JournalResponse.builder()
                .entries(entries)
                .typeSummary(summaryMap)
                .build();
    }

    @Override
    public MissionResponse getMissions(User user) {
        QWalletJournal journal = QWalletJournal.walletJournal;
        QCharacter character = QCharacter.character;

        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);

        List<JournalQueryDto> results = queryFactory
                .select(Projections.constructor(JournalQueryDto.class,
                        journal.date,
                        journal.refType,
                        journal.amount,
                        journal.balance,
                        journal.description,
                        journal.firstPartyId,
                        character.characterName))
                .from(journal)
                .join(journal.character, character)
                .where(character.user.id.eq(user.getId())
                        .and(journal.date.goe(thirtyDaysAgo))
                        .and(journal.refType.in("agent_mission_reward", "agent_mission_time_bonus")))
                .orderBy(journal.date.desc())
                .fetch();

        double totalIsk = results.stream()
                .mapToDouble(dto -> dto.getAmount() != null ? dto.getAmount() : 0.0)
                .sum();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 1. LP 이력 가져오기 및 날짜별 델타 집계
        List<LoyaltyPointHistory> lpHistory = loyaltyPointHistoryRepository.findByCharacterUserIdOrderByCreatedAtAsc(user.getId());
        
        Map<String, List<LoyaltyPointHistory>> lpGroupByCorp = lpHistory.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getCharacter().getCharacterId() + "_" + h.getCorporationId()
                ));

        Map<String, Integer> lpDeltasByDate = new HashMap<>();
        int totalLpChange = 0;
        LocalDateTime thirtyDaysAgoLocal = thirtyDaysAgo.toLocalDateTime();

        for (List<LoyaltyPointHistory> historyList : lpGroupByCorp.values()) {
            for (int i = 1; i < historyList.size(); i++) {
                LoyaltyPointHistory prev = historyList.get(i - 1);
                LoyaltyPointHistory curr = historyList.get(i);

                int delta = curr.getLoyaltyPoints() - prev.getLoyaltyPoints();
                if (delta == 0) continue;

                if (curr.getCreatedAt().isAfter(thirtyDaysAgoLocal)) {
                    String dateStr = curr.getCreatedAt().format(formatter);
                    lpDeltasByDate.put(dateStr, lpDeltasByDate.getOrDefault(dateStr, 0) + delta);
                    totalLpChange += delta;
                }
            }
        }
        /// lp 이력가져오기 끝

        Map<String, List<JournalQueryDto>> groupedByDate = results.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getDate().format(formatter),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MissionResponse.DailyMissionRecord> dailyRecords = groupedByDate.entrySet().stream()
                .map(entry -> {
                    String dateStr = entry.getKey();
                    List<JournalQueryDto> dayDtos = entry.getValue();

                    double dayIsk = dayDtos.stream()
                            .mapToDouble(dto -> dto.getAmount() != null ? dto.getAmount() : 0.0)
                            .sum();

                    long dayCount = dayDtos.stream()
                            .filter(dto -> "agent_mission_reward".equals(dto.getRefType()))
                            .map(dto -> dto.getFirstPartyId() + "_" + dto.getRefType() + "_" + dto.getAmount())
                            .distinct()
                            .count();

                    int lpEarned = lpDeltasByDate.getOrDefault(dateStr, 0);
                    double lpIskValue = lpEarned * 1000.0;

                    return MissionResponse.DailyMissionRecord.builder()
                            .date(dateStr)
                            .count((int) dayCount)
                            .iskIncome(dayIsk)
                            .lpEarned(lpEarned)
                            .totalIncome(dayIsk + lpIskValue)
                            .build();
                })
                .collect(Collectors.toList());

        int totalUniqueCount = dailyRecords.stream()
                .mapToInt(MissionResponse.DailyMissionRecord::getCount)
                .sum();

        return MissionResponse.builder()
                .stats(MissionResponse.MissionStats.builder()
                        .totalCount(totalUniqueCount)
                        .totalIsk(totalIsk)
                        .totalLp(totalLpChange)
                        .totalLpValue(totalLpChange * 1000.0)
                        .build())
                .dailyRecords(dailyRecords)
                .build();
    }

    private String mapRefType(String refType) {
        if (refType == null) return "ETC";
        String lower = refType.toLowerCase();
        
        if (lower.contains("bounty")) return "BOUNTY";
        if (lower.contains("market") || lower.contains("escrow") || lower.contains("broker")) return "MARKET";
        if (lower.contains("mission")) {
            if (lower.contains("bonus")) return "BONUS";
            return "MISSION";
        }
        if (lower.contains("tax")) return "TAX";
        
        return "ETC";
    }
}