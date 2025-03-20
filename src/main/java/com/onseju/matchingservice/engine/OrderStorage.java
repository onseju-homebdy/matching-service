package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.dto.TradeHistoryEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderStorage {

    private final ConcurrentSkipListSet<TradeOrder> elements = new ConcurrentSkipListSet<>(
            Comparator.comparing(TradeOrder::getCreatedDateTime)
    );

    public TradeHistoryEvent match(final TradeOrder incomingOrder) {
        TradeOrder matchingOrder = elements.first();
        BigDecimal matchedQuantity = incomingOrder.calculateMatchQuantity(matchingOrder);
        incomingOrder.decreaseRemainingQuantity(matchedQuantity);
        matchingOrder.decreaseRemainingQuantity(matchedQuantity);
        if (!matchingOrder.hasRemainingQuantity()) {
            elements.remove(matchingOrder);
        }
        return  createResponse(incomingOrder, matchingOrder, matchedQuantity);
    }

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
