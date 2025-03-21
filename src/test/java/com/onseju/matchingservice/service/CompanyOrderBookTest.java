package com.onseju.matchingservice.service;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.dto.TradeHistoryEvent;
import com.onseju.matchingservice.engine.CompanyOrderBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        void receiveLimitBuyOrder() {
            // given
            TradeOrder buyOrder = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("10"), 1L);

            // when, then
            assertThatNoException()
                    .isThrownBy(() -> orderBook.received(buyOrder));
        }

        @Test
        @DisplayName("지정가 매도 주문 추가")
        void receiveLimitSellOrder() {
            // given
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("10"), 1L);

            // when, then
            assertThatNoException()
                    .isThrownBy(() -> orderBook.received(sellOrder));
        }

        @Test
        @DisplayName("주문에 대한 체결이 완료된 경우 상태를 COMPLETE로 변경")
        void changeOrderStatusToCompleteWhenTradeIsExecuted() {
            // given
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);

            // when
            orderBook.received(sellOrder);
            orderBook.received(buyOrder);

            // then
            assertThat(sellOrder.getStatus()).isEqualTo(OrderStatus.COMPLETE);
            assertThat(buyOrder.getStatus()).isEqualTo(OrderStatus.COMPLETE);
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
                assertThat(result.sellAccountId()).isEqualTo(sellOrder.getAccountId());
                assertThat(result.buyOrderId()).isEqualTo(buyOrder1.getId());
                assertThat(result.buyAccountId()).isEqualTo(buyOrder1.getAccountId());
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
                assertThat(result.sellAccountId()).isEqualTo(sellOrder.getAccountId());
                assertThat(result.buyOrderId()).isEqualTo(buyOrder.getId());
                assertThat(result.buyAccountId()).isEqualTo(buyOrder.getAccountId());
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
        @DisplayName("매수 주문 불균형 상황 테스트")
        void buyOrderImbalance() {
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

        @Test
        @DisplayName("매도 주문 불균형 상황 테스트")
        void sellOrderImbalance() {
            // given
            for (int i = 0; i < 5; i++) {
                TradeOrder sellOrder = createOrder((long) i, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
                orderBook.received(sellOrder);
            }

            // when
            TradeOrder buyOrder = createOrder(5L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            Collection<TradeHistoryEvent> response = orderBook.received(buyOrder);

            // then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("시장가 주문 테스트")
    class MarketOrderTests {

        @Test
        @DisplayName("시장가 매수 주문 처리")
        void receiveMarketBuyOrder() {
            // given
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder marketBuyOrder = createOrder(2L, Type.MARKET_BUY, BigDecimal.ZERO, new BigDecimal("5"), 2L);


            // when
            List<TradeHistoryEvent> responses = new ArrayList<>();
            responses.addAll(orderBook.received(sellOrder));
            responses.addAll(orderBook.received(marketBuyOrder));

            // then
            assertThat(responses).hasSize(1);
            responses.forEach(res -> {
                assertThat(res.buyOrderId()).isEqualTo(marketBuyOrder.getId());
                assertThat(res.sellOrderId()).isEqualTo(sellOrder.getId());
                assertThat(res.quantity()).isEqualTo(marketBuyOrder.getTotalQuantity());
                assertThat(res.price()).isEqualTo(sellOrder.getPrice());
            });
        }

        @Test
        @DisplayName("시장가 매도 주문 처리")
        void receiveMarketSellOrder() {
            // given
            TradeOrder buyOrder = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder marketSellOrder = createOrder(2L, Type.MARKET_SELL, BigDecimal.ZERO, new BigDecimal("5"), 2L);


            // when
            orderBook.received(buyOrder);
            List<TradeHistoryEvent> responses = new ArrayList<>(orderBook.received(marketSellOrder));

            // then
            assertThat(responses).hasSize(1);
            responses.forEach(res -> {
                assertThat(res.buyOrderId()).isEqualTo(buyOrder.getId());
                assertThat(res.sellOrderId()).isEqualTo(marketSellOrder.getId());
                assertThat(res.quantity()).isEqualTo(marketSellOrder.getTotalQuantity());
                assertThat(res.price()).isEqualTo(buyOrder.getPrice());
            });
        }

        @Test
        @DisplayName("시장가 매수 주문 시, 낮은 가격의 매도 주문 부터 높은 가격의 주문 순으로 여러 가격대의 주문과 체결될 수 있다.")
        void matchBuyMarketOrderWithLowestPrice() {
            // given
            // 지정가 매도 주문 2개 추가 (서로 다른 가격)
            TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("49000"), new BigDecimal("5"), 1L);
            TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder marketBuyOrder = createOrder(3L, Type.MARKET_BUY, BigDecimal.ZERO, new BigDecimal("10"), 2L);


            // When
            orderBook.received(sellOrder1);
            orderBook.received(sellOrder2);
            List<TradeHistoryEvent> responses = orderBook.received(marketBuyOrder);

            // Then
            // 호가창 확인
            assertThat(responses).hasSize(2);

            TradeHistoryEvent sellOrderEvent1 = responses.get(0);
            assertThat(sellOrderEvent1.sellOrderId()).isEqualTo(sellOrder1.getId());
            assertThat(sellOrderEvent1.buyOrderId()).isEqualTo(marketBuyOrder.getId());
            assertThat(sellOrderEvent1.price()).isEqualTo(sellOrder1.getPrice());

            TradeHistoryEvent sellOrderEvent2 = responses.get(1);
            assertThat(sellOrderEvent2.sellOrderId()).isEqualTo(sellOrder2.getId());
            assertThat(sellOrderEvent2.buyOrderId()).isEqualTo(marketBuyOrder.getId());
            assertThat(sellOrderEvent2.price()).isEqualTo(sellOrder2.getPrice());
        }

        @Test
        @DisplayName("시장가 매수 주문 시, 낮은 가격의 매도 주문 부터 높은 가격의 주문 순으로 여러 가격대의 주문과 체결될 수 있다.")
        void matchSellMarketOrderWithHighestPrice() {
            // given
            // 지정가 매도 주문 2개 추가 (서로 다른 가격)
            TradeOrder buyOrder1 = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("49000"), new BigDecimal("5"), 1L);
            TradeOrder buyOrder2 = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L);
            TradeOrder marketSellOrder = createOrder(3L, Type.MARKET_SELL, BigDecimal.ZERO, new BigDecimal("10"), 2L);


            // When
            orderBook.received(buyOrder1);
            orderBook.received(buyOrder2);
            List<TradeHistoryEvent> responses = orderBook.received(marketSellOrder);

            // Then
            assertThat(responses).hasSize(2);

            // 낮은 가격의 주문 먼저 체결
            TradeHistoryEvent sellOrderEvent2 = responses.get(0);
            assertThat(sellOrderEvent2.sellOrderId()).isEqualTo(marketSellOrder.getId());
            assertThat(sellOrderEvent2.buyOrderId()).isEqualTo(buyOrder2.getId());
            assertThat(sellOrderEvent2.price()).isEqualTo(buyOrder2.getPrice());

            // 낮은 가격 체결 후 다음 가격대 주문 체결
            TradeHistoryEvent sellOrderEvent1 = responses.get(1);
            assertThat(sellOrderEvent1.sellOrderId()).isEqualTo(marketSellOrder.getId());
            assertThat(sellOrderEvent1.buyOrderId()).isEqualTo(buyOrder1.getId());
            assertThat(sellOrderEvent1.price()).isEqualTo(buyOrder1.getPrice());
        }
    }

    @Nested
    @DisplayName("주문 처리 조건 및 순서 테스트")
    class MatchingSequenceTests {

        @Test
        @DisplayName("매수 주문시, 같은 가격일 경우 먼저 주문이 들어온 주문부터 처리한다.")
        void buyOrderTimePriorityMatching() {
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            TradeOrder buyOrder1 = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L, createdAt);
            TradeOrder buyOrder2 = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L, createdAt.minusSeconds(1));
            TradeOrder sellOrder = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);

            orderBook.received(buyOrder2);
            orderBook.received(buyOrder1);
            orderBook.received(sellOrder);

            assertThat(buyOrder2.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
            assertThat(buyOrder1.getRemainingQuantity().get()).isEqualTo(new BigDecimal("5"));
            assertThat(sellOrder.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("매도 주문시, 같은 가격일 경우 먼저 주문이 들어온 주문부터 처리한다.")
        void sellOrderTimePriorityMatching() {
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L, createdAt.minusSeconds(1));
            TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L, createdAt);
            TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L, createdAt);

            orderBook.received(sellOrder1);
            orderBook.received(sellOrder2);
            orderBook.received(buyOrder);

            assertThat(sellOrder1.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
            assertThat(sellOrder2.getRemainingQuantity().get()).isEqualTo(new BigDecimal("5"));
            assertThat(buyOrder.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("매수 주문시, 모든 조건이 일치할 경우 수량이 많은 주문부터 체결한다.")
        void buyOrderQuantityPriorityMatching() {
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            TradeOrder buyOrder1 = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 1L, createdAt);
            TradeOrder buyOrder2 = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("10"), 1L, createdAt);
            TradeOrder sellOrder = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L, createdAt);

            orderBook.received(buyOrder1);
            orderBook.received(buyOrder2);
            List<TradeHistoryEvent> results = orderBook.received(sellOrder);

            assertThat(results.get(0).buyAccountId()).isEqualTo(buyOrder2.getAccountId());
            assertThat(results.get(0).sellAccountId()).isEqualTo(sellOrder.getAccountId());
            assertThat(buyOrder1.getRemainingQuantity().get()).isEqualTo(new BigDecimal(5));
            assertThat(buyOrder2.getRemainingQuantity().get()).isEqualTo(new BigDecimal(5));
            assertThat(sellOrder.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("매도 주문시, 모든 조건이 일치할 경우 수량이 많은 주문부터 체결한다.")
        void sellOrderQuantityPriorityMatching() {
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("10"), 1L, createdAt);
            TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L, createdAt);
            TradeOrder buyOrder = createOrder(3L, Type.MARKET_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L, createdAt);

            orderBook.received(sellOrder2);
            orderBook.received(sellOrder1);
            orderBook.received(buyOrder);

            assertThat(sellOrder1.getRemainingQuantity().get()).isEqualTo(new BigDecimal(5));
            assertThat(sellOrder2.getRemainingQuantity().get()).isEqualTo(new BigDecimal(5));
            assertThat(buyOrder.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("입력 받은 주문과 시장가를 비교한다")
    class MarketPrice {

        @Test
        @DisplayName("매도 주문시, 시장가보다 가격이 낮은 경우 true를 반환한다.")
        void isSellOrderBelowMarketPrice() {
            // given: 시장가 5만원 형성
            TradeOrder sellOrder1 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            TradeOrder sellOrder2 = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            orderBook.received(sellOrder1);
            orderBook.received(sellOrder2);

            // when
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("49000"), new BigDecimal("10"), 1L);
            boolean result = orderBook.isSellOrderBelowMarketPrice(sellOrder);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("매도 주문시, 시장가보다 가격이 높은 경우 false를 반환한다.")
        void isSellOrderHigherThanMarketPrice() {
            // given: 시장가 5만원 형성
            TradeOrder sellOrder1 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            TradeOrder sellOrder2 = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            orderBook.received(sellOrder1);
            orderBook.received(sellOrder2);

            // when
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("51000"), new BigDecimal("10"), 1L);
            boolean result = orderBook.isSellOrderBelowMarketPrice(sellOrder);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("매수 주문시, 시장가보다 가격이 높은 경우 true를 반환한다.")
        void isBuyOrderAboveMarketPrice() {
            // given: 시장가 5만원 형성
            TradeOrder buyOrder1 = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            TradeOrder buyOrder2 = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            orderBook.received(buyOrder1);
            orderBook.received(buyOrder2);

            // when
            TradeOrder buyOrder = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("51000"), new BigDecimal("10"), 1L);
            boolean result = orderBook.isBuyOrderAboveMarketPrice(buyOrder);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("매수 주문시, 시장가보다 가격이 낮은 경우 false를 반환한다.")
        void isBuyOrderLowerThanMarketPrice() {
            // given: 시장가 5만원 형성
            TradeOrder buyOrder1 = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            TradeOrder buyOrder2 = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);
            orderBook.received(buyOrder1);
            orderBook.received(buyOrder2);

            // when
            TradeOrder buyOrder = createOrder(1L, Type.LIMIT_BUY, new BigDecimal("49000"), new BigDecimal("10"), 1L);
            boolean result = orderBook.isBuyOrderAboveMarketPrice(buyOrder);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("자신의 주문과는 매칭되지 않는다.")
        void changeOrderStatusToCompleteWhenTradeIsExecuted() {
            // given
            Long accountId = 1L;
            TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), accountId);
            TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), accountId);

            // when
            orderBook.received(sellOrder);
            orderBook.received(buyOrder);

            // then
            assertThat(sellOrder.getStatus()).isNotEqualTo(OrderStatus.COMPLETE);
            assertThat(buyOrder.getStatus()).isNotEqualTo(OrderStatus.COMPLETE);
        }
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

    private TradeOrder createOrder(Long id, Type type, BigDecimal price, BigDecimal quantity, Long accountId, LocalDateTime createdDateTime) {
        return TradeOrder.builder()
                .id(id)
                .type(type)
                .price(price)
                .accountId(accountId)
                .companyCode("005930")
                .status(OrderStatus.ACTIVE)
                .totalQuantity(quantity)
                .remainingQuantity(new AtomicReference<>(quantity))
                .createdDateTime(createdDateTime)
                .build();
    }
}
