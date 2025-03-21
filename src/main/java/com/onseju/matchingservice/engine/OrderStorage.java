package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.dto.TradeHistoryEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderStorage {

    private final ConcurrentSkipListSet<TradeOrder> elements = new ConcurrentSkipListSet<>(
            Comparator.comparing(TradeOrder::getCreatedDateTime)
                    .thenComparing(TradeOrder::getTotalQuantity, Comparator.reverseOrder())
                    .thenComparing(TradeOrder::getId)
    );

    // Set 내에 존재하는 주문과 입력된 주문을 매칭힌다.
    public List<TradeHistoryEvent> match(final TradeOrder incomingOrder) {
        Iterator<TradeOrder> iterator = elements.iterator();
        List<TradeHistoryEvent> results = new ArrayList<>();
        while (iterator.hasNext() && incomingOrder.hasRemainingQuantity()) {
            final TradeOrder foundedOrder = iterator.next();
            if (foundedOrder.isSameAccount(incomingOrder.getAccountId())) {
                continue;
            }

            BigDecimal matchedQuantity = incomingOrder.calculateMatchQuantity(foundedOrder);
            // 체결 완료 후 남은 수량 감소 및 완료 여부 확인
            incomingOrder.decreaseRemainingQuantity(matchedQuantity);
            foundedOrder.decreaseRemainingQuantity(matchedQuantity);
            incomingOrder.checkAndChangeOrderStatus();
            foundedOrder.checkAndChangeOrderStatus();
            results.add(createResponse(incomingOrder, foundedOrder, matchedQuantity));

            if (!foundedOrder.hasRemainingQuantity())
                iterator.remove();
        }
        return results;
    }

    // 매칭 완료 후 응답 생성
    private TradeHistoryEvent createResponse(final TradeOrder incomingOrder, final TradeOrder foundOrder, BigDecimal matchedQuantity) {
        final BigDecimal price = getMatchingPrice(incomingOrder, foundOrder);
        if (incomingOrder.isSellType()) {
            return new TradeHistoryEvent(
                    incomingOrder.getCompanyCode(),
                    foundOrder.getId(),
                    incomingOrder.getId(),
                    matchedQuantity,
                    price,
                    Instant.now().getEpochSecond()
            );
        }
        return new TradeHistoryEvent(
                incomingOrder.getCompanyCode(),
                incomingOrder.getId(),
                foundOrder.getId(),
                matchedQuantity,
                price,
                Instant.now().getEpochSecond()
        );
    }

    // 매칭 가격을 계산한다.
    private BigDecimal getMatchingPrice(final TradeOrder incomingOrder, final TradeOrder foundOrder) {
        if (incomingOrder.isMarketOrder()) {
            return foundOrder.getPrice();
        }
        return incomingOrder.getPrice();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void add(TradeOrder order) {
        elements.add(order);
    }
}
