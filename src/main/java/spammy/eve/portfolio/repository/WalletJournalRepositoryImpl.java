package spammy.eve.portfolio.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.portfolio.domain.QCharacter;
import spammy.eve.portfolio.domain.QWalletJournal;
import spammy.eve.portfolio.dto.JournalQueryDto;
import spammy.eve.portfolio.response.JournalResponse;
import spammy.eve.portfolio.response.MissionResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import spammy.eve.portfolio.domain.User;

@RequiredArgsConstructor
public class WalletJournalRepositoryImpl implements WalletJournalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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

        double totalIsk = 0;
        int totalUniqueCount = 0;
        Set<String> processedEvents = new HashSet<>();
        Map<String, MissionResponse.DailyMissionRecord> dailyMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (JournalQueryDto dto : results) {
            double amount = dto.getAmount() != null ? dto.getAmount() : 0.0;
            totalIsk += amount;

            String dateStr = dto.getDate().format(formatter);
            
            // 고유 사건 키: 에이전트ID_초단위타임스탬프
            String eventKey = dto.getFirstPartyId() + "_" + dto.getDate().toEpochSecond();
            int countIncrement = 0;
            
            if (processedEvents.add(eventKey)) {
                countIncrement = 1;
                totalUniqueCount++;
            }

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
                    .count(record.getCount() + countIncrement)
                    .iskIncome(record.getIskIncome() + amount)
                    .lpEarned(0) // LP 정보는 현재 없으므로 0
                    .totalIncome(record.getTotalIncome() + amount)
                    .build());
        }

        return MissionResponse.builder()
                .stats(MissionResponse.MissionStats.builder()
                        .totalCount(totalUniqueCount)
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