package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.factory.OrderBookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class MatchingEngineTest {

    @InjectMocks
    private MatchingEngine matchingEngine;

    private OrderBookFactory orderBookFactory = new OrderBookFactory();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngine(orderBookFactory, eventPublisher);
    }

    @Test
    @DisplayName("주문 처리 시 주문장이 없으면 생성한다")
    void processOrderShouldCreateOrderBookIfNotExists() {
        // given
        TradeOrder order = createOrder(1L, Type.LIMIT_SELL, new BigDecimal(10000), new BigDecimal(5), 1L);

        // when
        assertThatNoException()
                .isThrownBy(() -> matchingEngine.processOrder(order));
    }

    @Test
    @DisplayName("매도 주문이 시장가보다 낮으면 시장가 주문으로 변경한다")
    void convertSellOrderToMarketIfBelowMarketPrice() {
        // given
        TradeOrder order = createOrder(1L, Type.LIMIT_SELL, new BigDecimal(10000), new BigDecimal(5), 1L);
        TradeOrder order2 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal(10000), new BigDecimal(5), 1L);
        TradeOrder order3 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal(9000), new BigDecimal(5), 1L);

        // when
        // 시장가 10,000원 형성
        matchingEngine.processOrder(order);
        matchingEngine.processOrder(order2);

        // 시장가 보다 낮은 가격(9,000원)의 매도 주문 추가
        matchingEngine.processOrder(order3);

        // then
        assertThat(order3.getType()).isEqualTo(Type.MARKET_SELL);
    }

    @Test
    @DisplayName("매수 주문이 시장가보다 높으면 시장가 주문으로 변경한다")
    void processOrder_shouldConvertBuyOrderToMarketIfAboveMarketPrice() {
        // given
        TradeOrder order = createOrder(1L, Type.LIMIT_BUY, new BigDecimal(10000), new BigDecimal(5), 1L);
        TradeOrder order2 = createOrder(1L, Type.LIMIT_BUY, new BigDecimal(10000), new BigDecimal(5), 1L);
        TradeOrder order3 = createOrder(1L, Type.LIMIT_BUY, new BigDecimal(11000), new BigDecimal(5), 1L);

        // when
        // 시장가 10,000원 형성
        matchingEngine.processOrder(order);
        matchingEngine.processOrder(order2);

        // 시장가보다 높은 가격(11,000원)의 매수 주문 추가
        matchingEngine.processOrder(order3);

        // then
        assertThat(order3.getType()).isEqualTo(Type.MARKET_BUY);
    }

    private TradeOrder createOrder(Long id, Type type, BigDecimal price, BigDecimal quantity, Long accountId) {
        return TradeOrder.builder()
                .id(id)
                .type(type)
                .price(price)
                .accountId(accountId)
                .companyCode("005930")
                .status(OrderStatus.ACTIVE)
                .totalQuantity(quantity)
                .remainingQuantity(new AtomicReference<>(quantity))
                .createdDateTime(LocalDateTime.of(2025, 03, 01, 0, 0, 0))
                .build();
    }
}
