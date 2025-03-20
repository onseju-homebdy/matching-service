package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.dto.TradeHistoryEvent;
import com.onseju.matchingservice.factory.OrderBookFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MatchingEngine {

    // 종목 코드를 키로 하는 주문들
    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final OrderBookFactory orderBookFactory;
    private final ApplicationEventPublisher eventPublisher;

    public void processOrder(final TradeOrder order) {
        final OrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
        Collection<TradeHistoryEvent> results = orderBook.received(order);
        results.forEach(eventPublisher::publishEvent);
    }

    // 종목별 주문장 생성, 이미 존재할 경우 반환
    private OrderBook getOrCreateOrderBook(final String companyCode) {
        return orderBooks.computeIfAbsent(
                companyCode,
                key -> orderBookFactory.createOrderBook()
        );
    }
}
