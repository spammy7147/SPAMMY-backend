package spammy.eve.character.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.character.domain.QCharacter;
import spammy.eve.character.domain.QWalletJournal;
import spammy.eve.character.dto.JournalResponse;
import spammy.eve.character.dto.MissionResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WalletJournalRepositoryImpl implements WalletJournalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public JournalResponse getJournal(Long userId) {
        QWalletJournal journal = QWalletJournal.walletJournal;
        QCharacter character = QCharacter.character;

        List<Tuple> results = queryFactory
                .select(journal, character.characterName)
                .from(journal)
                .join(journal.character, character)
                .where(character.user.id.eq(userId))
                .orderBy(journal.date.desc())
                .limit(1000) // 최근 1000건만
                .fetch();

        List<JournalResponse.JournalEntry> entries = new ArrayList<>();
        Map<String, JournalResponse.TypeSummary> summaryMap = new HashMap<>();

        for (Tuple t : results) {
            spammy.eve.character.domain.WalletJournal wj = t.get(journal);
            String charName = t.get(character.characterName);
            String mappedType = mapRefType(wj.getRefType());

            entries.add(JournalResponse.JournalEntry.builder()
                    .date(wj.getDate())
                    .charName(charName)
                    .type(mappedType)
                    .desc(wj.getDescription())
                    .amount(wj.getAmount())
                    .balance(wj.getBalance())
                    .build());

            JournalResponse.TypeSummary summary = summaryMap.computeIfAbsent(mappedType, 
                k -> JournalResponse.TypeSummary.builder().count(0).total(0.0).build());
            
            // summaryMap의 value는 immutable(Builder로 생성됨)이므로 새로 생성해야 함
            summaryMap.put(mappedType, JournalResponse.TypeSummary.builder()
                    .count(summary.getCount() + 1)
                    .total(summary.getTotal() + (wj.getAmount() != null ? wj.getAmount() : 0.0))
                    .build());
        }

        return JournalResponse.builder()
                .entries(entries)
                .typeSummary(summaryMap)
                .build();
    }

    @Override
    public MissionResponse getMissions(Long userId) {
        QWalletJournal journal = QWalletJournal.walletJournal;
        QCharacter character = QCharacter.character;

        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);

        List<Tuple> results = queryFactory
                .select(journal, character.characterName)
                .from(journal)
                .join(journal.character, character)
                .where(character.user.id.eq(userId)
                        .and(journal.date.goe(thirtyDaysAgo))
                        .and(journal.refType.in("agent_mission_reward", "agent_mission_time_bonus")))
                .orderBy(journal.date.desc())
                .fetch();

        double totalIsk = 0;
        int totalCount = results.size();
        Map<String, MissionResponse.DailyMissionRecord> dailyMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Tuple t : results) {
            spammy.eve.character.domain.WalletJournal wj = t.get(journal);
            double amount = wj.getAmount() != null ? wj.getAmount() : 0.0;
            totalIsk += amount;

            String dateStr = wj.getDate().format(formatter);
            MissionResponse.DailyMissionRecord record = dailyMap.computeIfAbsent(dateStr, 
                k -> MissionResponse.DailyMissionRecord.builder()
                        .date(k)
                        .count(0)
                        .iskIncome(0.0)
                        .lpEarned(0)
                        .totalIncome(0.0)
                        .build());
            
            dailyMap.put(dateStr, MissionResponse.DailyMissionRecord.builder()
                    .date(dateStr)
                    .count(record.getCount() + 1)
                    .iskIncome(record.getIskIncome() + amount)
                    .lpEarned(0) // LP 정보는 현재 없으므로 0
                    .totalIncome(record.getTotalIncome() + amount)
                    .build());
        }

        return MissionResponse.builder()
                .stats(MissionResponse.MissionStats.builder()
                        .totalCount(totalCount)
                        .totalIsk(totalIsk)
                        .totalLp(0)
                        .totalLpValue(0.0)
                        .build())
                .dailyRecords(new ArrayList<>(dailyMap.values()))
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