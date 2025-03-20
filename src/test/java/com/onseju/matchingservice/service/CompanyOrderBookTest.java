package com.onseju.matchingservice.service;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.dto.TradeHistoryEvent;
import com.onseju.matchingservice.engine.CompanyOrderBook;
import com.onseju.matchingservice.exception.MatchingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

public class CompanyOrderBookTest {

    private CompanyOrderBook orderBook;

    @BeforeEach
    public void init() {
        orderBook = new CompanyOrderBook();
    }

    @Nested
	@DisplayName("지정가 주문 테스트")
	class LimitOrderTests {
        @Test
        @DisplayName("지정가 매수 주문 추가")
        void receiveLimitBuyOrder() throws MatchingException {
            // given
            TradeOrder buyOrder = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("10"), 1L);

            // when, then
            assertThatNoException()
                    .isThrownBy(() -> orderBook.received(buyOrder));
        }

        @Test
        @DisplayName("지정가 매도 주문 추가")
        void receiveLimitSellOrder() throws MatchingException {
            // given
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("10"), 1L);

            // when, then
            assertThatNoException()
                    .isThrownBy(() -> orderBook.received(sellOrder));
        }

        @Test
        @DisplayName("지정가 매수 주문 시, 일치하는 가격의 매도 주문과 체결된다.")
        void matchBuyOrderWithSamePriceSellOrder() {
            // given
            TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("49000"), new BigDecimal("5"), 1L);
            TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);

            // when
            orderBook.received(sellOrder1);
            orderBook.received(sellOrder2);
            Collection<TradeHistoryEvent> responses = orderBook.received(buyOrder);

            // then
            assertThat(responses).hasSize(1);
            responses.forEach(result -> {
                assertThat(result.sellOrderId()).isEqualTo(sellOrder1.getId());
                assertThat(result.buyOrderId()).isEqualTo(buyOrder.getId());
                assertThat(result.price()).isEqualTo(buyOrder.getPrice());
                assertThat(result.quantity()).isEqualTo(buyOrder.getTotalQuantity());
            });
        }

        @Test
        @DisplayName("지정가 매도 주문 시, 일치하는 가격의 매수 주문과 체결된다.")
        void matchSellOrderWithSamePriceBuyOrder() {
            // given
            TradeOrder buyOrder1 = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder buyOrder2 = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("49000"), new BigDecimal("5"), 1L);
            TradeOrder sellOrder = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);

            // when
            orderBook.received(buyOrder1);
            orderBook.received(buyOrder2);
            Collection<TradeHistoryEvent> responses = orderBook.received(sellOrder);

            // then
            assertThat(responses).hasSize(1);
            responses.forEach(result -> {
                assertThat(result.sellOrderId()).isEqualTo(sellOrder.getId());
                assertThat(result.buyOrderId()).isEqualTo(buyOrder1.getId());
                assertThat(result.price()).isEqualTo(sellOrder.getPrice());
                assertThat(result.quantity()).isEqualTo(sellOrder.getTotalQuantity());
            });
        }

        @Test
        @DisplayName("지정가 매수 주문 시, 일치하는 가격의 매도 주문과 부분 체결될 수 있다.")
        void matchPartialSellOrderWithSamePriceSellOrder() {
            // given
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder buyOrder = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("10"), 2L);

            // when
            orderBook.received(sellOrder);
            Collection<TradeHistoryEvent> responses = orderBook.received(buyOrder);

            // then
            assertThat(responses).hasSize(1);
            assertThat(buyOrder.getRemainingQuantity().get()).isEqualTo(new BigDecimal("5"));
            responses.forEach(result -> {
                assertThat(result.sellOrderId()).isEqualTo(sellOrder.getId());
                assertThat(result.buyOrderId()).isEqualTo(buyOrder.getId());
                assertThat(result.price()).isEqualTo(buyOrder.getPrice());
                assertThat(result.quantity()).isEqualTo(buyOrder.getTotalQuantity().subtract(buyOrder.getRemainingQuantity().get()));
            });
        }


        @Test
        @DisplayName("지정가 매도 주문 시, 일치하는 가격의 매수 주문과 부분 체결될 수 있다.")
        void testPartialOrderExecution() {
            // given
            TradeOrder buyOrder = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("10"), 2L);

            // when
            orderBook.received(buyOrder);
            Collection<TradeHistoryEvent> responses = orderBook.received(sellOrder);

            // then
            assertThat(responses).hasSize(1);
            assertThat(sellOrder.getRemainingQuantity().get()).isEqualTo(new BigDecimal("5"));
            responses.forEach(result -> {
                assertThat(result.sellOrderId()).isEqualTo(sellOrder.getId());
                assertThat(result.buyOrderId()).isEqualTo(buyOrder.getId());
                assertThat(result.price()).isEqualTo(buyOrder.getPrice());
                assertThat(result.quantity()).isEqualTo(buyOrder.getTotalQuantity().subtract(buyOrder.getRemainingQuantity().get()));
            });
        }


        @Test
        @DisplayName("주문 불균형 상황 테스트")
        void testOrderImbalance() {
            // given
            for (int i = 0; i < 5; i++) {
                TradeOrder buyOrder = createOrder((long) i, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L);
                orderBook.received(buyOrder);
            }

            // when
            TradeOrder sellOrder = createOrder(5L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            Collection<TradeHistoryEvent> response = orderBook.received(sellOrder);

            // then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("시장가 주문 테스트")
    class MarketOrderTests {


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

    // 매도자, 매수자 구분 후 응답 생성
    private TradeHistoryEvent createResponse(final TradeOrder incomingOrder, final TradeOrder foundOrder, BigDecimal matchedQuantity, BigDecimal matchPrice) {
        if (incomingOrder.isSellType()) {
            return new TradeHistoryEvent(
                    incomingOrder.getCompanyCode(),
                    foundOrder.getId(),
                    incomingOrder.getId(),
                    matchedQuantity,
                    incomingOrder.getPrice(),
                    Instant.now().getEpochSecond()
            );
        }
        return new TradeHistoryEvent(
                incomingOrder.getCompanyCode(),
                incomingOrder.getId(),
                foundOrder.getId(),
                matchedQuantity,
                incomingOrder.getPrice(),
                Instant.now().getEpochSecond()
        );
    }

}
