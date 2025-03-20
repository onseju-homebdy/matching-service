package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.dto.TradeHistoryEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 종복별로 주문을 관리한다.
 */
@Slf4j
public class CompanyOrderBook implements OrderBook {

    // 매도 주문: 낮은 가격 우선
    private final ConcurrentSkipListMap<Price, OrderStorage> sellOrders = new ConcurrentSkipListMap<>(
            Comparator.comparing(Price::getValue)
    );

    // 매수 주문: 높은 가격 우선
    private final ConcurrentSkipListMap<Price, OrderStorage> buyOrders = new ConcurrentSkipListMap<>(
            Comparator.comparing(Price::getValue).reversed()
    );

    /**
     * 주문을 시장가, 지정가로 나누어 처리한다.
     */
    @Override
    public List<TradeHistoryEvent> received(final TradeOrder order) {
        if (order.isMarketOrder()) {
            return processMarketOrder(order);
        }
        return processLimitOrder(order);
    }

    /**
     * 시장가 주문: 주문을 매칭한 후, 남은 수량에 대한 매칭을 더 이상 진행하지 않는다.
     */
    private List<TradeHistoryEvent> processMarketOrder(final TradeOrder order) {
        if (order.isSellType()) {
            List<TradeHistoryEvent> results = new ArrayList<>();
            for (Price now: buyOrders.keySet()) {
                Collection<TradeHistoryEvent> responses = match(now, order);
                results.addAll(
                        responses.stream()
                                .filter(Objects::nonNull)
                                .toList()
                );
            }
            return results;
        }
        List<TradeHistoryEvent> results = new ArrayList<>();
        for (Price now: sellOrders.keySet()) {
            Collection<TradeHistoryEvent> responses = match(now, order);
            results.addAll(
                    responses.stream()
                            .filter(Objects::nonNull)
                            .toList()
            );
        }
        return results;
    }

    /**
     * 지정가 주문: 주문을 매칭한 후, 남은 수량을 OrderStorage에 추가한다.
     */
    private List<TradeHistoryEvent> processLimitOrder(final TradeOrder order) {
        final Price now = new Price(order.getPrice());
        List<TradeHistoryEvent> result = match(now, order);
        if (order.hasRemainingQuantity()) {
            addRemainingTradeOrder(order);
        }
        return result;
    }

    private List<TradeHistoryEvent> match(final Price price, final TradeOrder order) {
        final List<TradeHistoryEvent> results = new ArrayList<>();
        while (order.hasRemainingQuantity()) {
            final OrderStorage orderStorage = getCounterOrderStorage(price, order.getType());
            if (orderStorage == null || orderStorage.isEmpty()) {
                break;
            }
            TradeHistoryEvent history = orderStorage.match(order);
            results.add(history);
        }
        return results;
    }

    private OrderStorage getOrCreateSameTypeOrderStorage(final Price price, final Type type) {
        if (type.isSell()) {
            if (sellOrders.containsKey(price)) {
                return sellOrders.get(price);
            }
            sellOrders.put(price, new OrderStorage());
            return sellOrders.get(price);
        }
        if (buyOrders.containsKey(price)) {
            return buyOrders.get(price);
        }
        buyOrders.put(price, new OrderStorage());
        return buyOrders.get(price);
    }

    private OrderStorage getCounterOrderStorage(final Price price, final Type type) {
        if (type.isSell()) {
            return buyOrders.get(price);
        }
        return sellOrders.get(price);
    }

    private void addRemainingTradeOrder(final TradeOrder order) {
        Price price = new Price(order.getPrice());
        if (order.isSellType()) {
            OrderStorage orderStorage = getOrCreateSameTypeOrderStorage(price, order.getType());
            orderStorage.add(order);
            return;
        }
        OrderStorage orderStorage = getOrCreateSameTypeOrderStorage(price, order.getType());
        orderStorage.add(order);
    }

    @Override
    public boolean isSellOrderBelowMarketPrice(TradeOrder order) {
        return false;
    }

    @Override
    public boolean isBuyOrderAboveMarketPrice(TradeOrder order) {
        return false;
    }
}
