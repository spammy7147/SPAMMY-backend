package spammy.eve.portfolio.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.portfolio.domain.QCharacter;
import spammy.eve.portfolio.domain.QMarketOrder;
import spammy.eve.portfolio.response.OrderResponse;
import spammy.eve.sde.QType;

import java.util.ArrayList;
import java.util.List;
import spammy.eve.portfolio.domain.User;

@RequiredArgsConstructor
public class MarketOrderRepositoryImpl implements MarketOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public OrderResponse getOrders(User user) {
        QMarketOrder order = QMarketOrder.marketOrder;
        QCharacter character = QCharacter.character;
        QType type = QType.type;

        List<Tuple> results = queryFactory
                .select(order, character.characterName, type.nameEn)
                .from(order)
                .join(order.character, character)
                .leftJoin(type).on(order.typeId.eq(type.id))
                .where(character.user.id.eq(user.getId()))
                .orderBy(order.issued.desc())
                .fetch();


        List<OrderResponse.OrderEntry> entries = new ArrayList<>();
        for (Tuple t : results) {
            spammy.eve.portfolio.domain.MarketOrder mo = t.get(order);
            String charName = t.get(character.characterName);
            String typeName = t.get(type.nameEn) != null ? t.get(type.nameEn) : "Unknown Item (" + mo.getTypeId() + ")";

            entries.add(OrderResponse.OrderEntry.builder()
                    .orderId(mo.getOrderId())
                    .charName(charName)
                    .typeName(typeName)
                    .typeId(mo.getTypeId())
                    .price(mo.getPrice())
                    .volumeRemain(mo.getVolumeRemain())
                    .volumeTotal(mo.getVolumeTotal())
                    .isBuyOrder(mo.getIsBuyOrder())
                    .locationName("Location ID: " + mo.getLocationId())
                    .issued(mo.getIssued())
                    .state(mo.getState())
                    .build());
        }

        return OrderResponse.builder()
                .entries(entries)
                .build();
    }
}
