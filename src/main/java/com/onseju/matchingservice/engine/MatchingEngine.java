package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.dto.TradeHistoryEvent;
import com.onseju.matchingservice.factory.OrderBookFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    // 종목 코드를 키로 하는 주문들
    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final OrderBookFactory orderBookFactory;
    private final ApplicationEventPublisher eventPublisher;

    public void processOrder(final TradeOrder order) {
        final OrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
        checkAndChangeLimitToMarket(order);
        List<TradeHistoryEvent> results = orderBook.received(order);
        results.forEach(i -> log.info("체결 완료: sell order - " + i.sellOrderId() + ", buyOrderId - " + i.buyOrderId()));
        results.forEach(eventPublisher::publishEvent);
    }

    // 종목별 주문장 생성, 이미 존재할 경우 반환
    private OrderBook getOrCreateOrderBook(final String companyCode) {
        return orderBooks.computeIfAbsent(
                companyCode,
                key -> orderBookFactory.createOrderBook()
        );
    }

    // 지정가 주문 시, 시장가와 비교하여 시장가보다 불리할 경우 시장가로 상태 변경
    private void checkAndChangeLimitToMarket(final TradeOrder order) {
        if (order.isSellType()) {
            updateSellOrderStatusIfBelowMarketPrice(order);
            return;
        }
        updateBuyOrderStatusIfAboveMarketPrice(order);
    }

    // 매도 주문의 가격이 시장가보다 낮은 경우 시장가로 상태 변경
    private void updateSellOrderStatusIfBelowMarketPrice(final TradeOrder order) {
        OrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
        if (orderBook.isSellOrderBelowMarketPrice(order)) {
            order.changeTypeToMarket();
        }
    }

    // 매수 주문의 가격의 시장가보다 높은 경우 시장가로 상태 변경
    private void updateBuyOrderStatusIfAboveMarketPrice(final TradeOrder order) {
        OrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
        if (orderBook.isBuyOrderAboveMarketPrice(order)) {
            order.changeTypeToMarket();
        }
    }
}
