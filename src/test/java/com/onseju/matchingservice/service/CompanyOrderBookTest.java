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

//
//        @Test
//        @DisplayName("TC8.1.5 부분 체결 테스트")
//        void testPartialOrderExecution() {
//            // Given
//            // 서로 다른 가격의 매도 주문 2개 추가
//            TradeOrder sellOrder1 = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//            TradeOrder sellOrder2 = createOrder(Type.SELL, new BigDecimal("50500"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//            orderBook.received(sellOrder1);
//            orderBook.received(sellOrder2);
//
//            // When
//            // 매수 주문 추가 (매도 주문보다 높은 가격, 매도 주문 총량보다 적은 수량)
//            TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("51000"), new BigDecimal("7"), OrderStatus.ACTIVE, account2);
//            orderBook.received(buyOrder);
//
//            // Then
//            // 호가창 확인
//            OrderBookResponse afterExecutionOrderBook = orderBook.getBook();
//
//            // 매도 호가 확인
//            List<PriceLevelDto> sellLevels = afterExecutionOrderBook.sellLevels();
//            assertEquals(1, sellLevels.size(), "매도 호가가 1개만 남아 있어야 함");
//            assertEquals(new BigDecimal("50500"), sellLevels.get(0).price(), "남은 매도 호가는 높은 가격의 호가여야 함");
//            assertEquals(new BigDecimal("8"), sellLevels.get(0).quantity(), "남은 매도 수량이 8이어야 함 (원래 10개에서 2개 체결)");
//
//            // 매수 호가 확인 - 모두 체결되어 없어야 함
//            List<PriceLevelDto> buyLevels = afterExecutionOrderBook.buyLevels();
//            assertEquals(0, buyLevels.size(), "매수 호가가 없어야 함 (모두 체결됨)");
//        }
//
//        @Test
//        @DisplayName("TC3.1.2 & TC3.2.2 부분 체결")
//        void testPartialExecution() throws MatchingException {
//            // given
//            TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//            TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account2);
//
//            // when
//            List<TradeHistoryResponse> responses = new ArrayList<>();
//            responses.addAll(orderBook.received(buyOrder));
//            responses.addAll(orderBook.received(sellOrder));
//
//            // then
//            OrderBookResponse response = orderBook.getBook();
//            assertFalse(response.buyLevels().isEmpty());
//            assertTrue(response.sellLevels().isEmpty());
//            assertThat(responses.get(0).quantity()).isEqualTo(new BigDecimal("5"));
//        }
//
//        @Test
//        @DisplayName("TC3.3 주문 불균형 상황 테스트")
//        @Transactional
//        void testOrderImbalance() {
//            // 다수의 매수 주문 생성
//            for (int i = 0; i < 5; i++) {
//                TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//                orderBook.received(buyOrder);
//            }
//
//            // 소수의 매도 주문 생성
//            TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account2);
//            orderBook.received(sellOrder);
//
//            OrderBookResponse response = orderBook.getBook();
//            assertFalse(response.buyLevels().isEmpty());
//            assertTrue(response.sellLevels().isEmpty());
//            assertEquals(new BigDecimal("45"), response.buyLevels().get(0).quantity());
//        }
//
//        @Test
//        @DisplayName("TC3.4 대량 주문 처리 테스트")
//        void testLargeOrderProcessing() throws MatchingException {
//            BigDecimal largeQuantity = new BigDecimal("1000000");
//            TradeOrder largeBuyOrder = createOrder(Type.BUY, new BigDecimal("50000"), largeQuantity, OrderStatus.ACTIVE, account1);
//            orderBook.received(largeBuyOrder);
//
//            TradeOrder smallSellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("100"),
//                    OrderStatus.ACTIVE, account2);
//            orderBook.received(smallSellOrder);
//
//            OrderBookResponse response = orderBook.getBook();
//            assertFalse(response.buyLevels().isEmpty());
//            assertTrue(response.sellLevels().isEmpty());
//            assertEquals(largeQuantity.subtract(new BigDecimal("100")), response.buyLevels().get(0).quantity());
//        }
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
