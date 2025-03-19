package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchingEngine {

    // 종목 코드를 키로 하는 주문들
    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();


    public void processOrder(final TradeOrder order) {
        final OrderBook orderBook = addOrderBook(order.getCompanyCode());
        orderBook.received(order);
    }

    // 종목별 주문장 생성, 이미 존재할 경우 반환
    public OrderBook addOrderBook(final String companyCode) {
        return orderBooks.computeIfAbsent(companyCode, k -> new OrderBook(companyCode));
    }
}
