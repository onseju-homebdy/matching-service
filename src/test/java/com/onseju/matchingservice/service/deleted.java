//package com.onseju.matchingservice.service;
//
//import com.onseju.matchingservice.MatchingException;
//import com.onseju.matchingservice.domain.OrderStatus;
//import com.onseju.matchingservice.domain.TradeOrder;
//import com.onseju.matchingservice.domain.Type;
//import com.onseju.matchingservice.engine.OrderBook;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatNoException;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class OrderBookTest {
//
//	private static final Logger log = LoggerFactory.getLogger(OrderBookTest.class);
//	private OrderBook orderBook;
//
//	private final String COMPANY_CODE = "005930";
//
//	@Nested
//	@DisplayName("지정가 주문 테스트")
//	class LimitOrderTests {
//		@Test
//		@DisplayName("TC3.1.1 지정가 매수 주문 추가")
//		void testReceiveLimitBuyOrder() throws MatchingException {
//			// given
//			TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//
//			// when, then
//			assertThatNoException()
//					.isThrownBy(() -> orderBook.received(buyOrder));
//		}
//
//		@Test
//		@DisplayName("TC3.2.1 지정가 매도 주문 추가")
//		void testReceiveLimitSellOrder() throws MatchingException {
////			// given
////			TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
////
////			// when, then
////			assertThatNoException()
////					.isThrownBy(() -> orderBook.received(sellOrder));
////		}
////
////		@Test
////		@DisplayName("지정가 매도 주문 시, 일치하는 가격 또는 높은 가격의 매수 주문과 체결된다.")
////		void matchSellOrderWithHigherPriceBuyOrder() {
////			// given
////			TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("51000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
////			TradeOrder buyOrder2 = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
////			TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account2);
////
////			// when
////			responses.addAll(orderBook.received(buyOrder));
////			responses.addAll(orderBook.received(buyOrder2));
////			responses.addAll(orderBook.received(sellOrder));
////
////			// then
////			assertThat(responses).hasSize(2);
////			assertThat(responses.get(0).price()).isEqualTo(buyOrder.getPrice());
////			assertThat(responses.get(1).quantity()).isEqualTo(new BigDecimal("5"));
////			assertThat(responses.get(1).price()).isEqualTo(buyOrder2.getPrice());
////		}
////
////
//		// TODO: 지정가 체결 방식 토의 필요(완전 매칭되는 가격부터 vs 최저 가격부터)
//		@Test
//		@DisplayName("지정가 매수 주문 시, 일치하는 가격 또는 더 낮은 가격의 매도 주문과 체결된다.")
//		void matchBuyOrderWithLowerPriceBuyOrder() {
//			// given
//			TradeOrder sellOrder1 = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(Type.SELL, new BigDecimal("49000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account2);
//
//			// when
//			List<TradeHistoryResponse> responses = new ArrayList<>();
//			responses.addAll(orderBook.received(sellOrder1));
//			responses.addAll(orderBook.received(sellOrder2));
//			responses.addAll(orderBook.received(buyOrder));
//
//			// then
//			assertThat(responses).hasSize(2);
//			assertThat(responses.get(0).price()).isEqualTo(sellOrder2.getPrice());
//			assertThat(responses.get(1).quantity()).isEqualTo(new BigDecimal("5"));
//			assertThat(responses.get(1).price()).isEqualTo(sellOrder1.getPrice());
//		}
//
////		@Test
////		@DisplayName("지정가 매수 주문 시, 일치하는 가격 또는 더 낮은 가격의 매도 주문과 체결된다.")
////		void matchBuyOrderWithLowerPriceBuyOrder() {
////			// given
////			TradeOrder sellOrder1 = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
////			TradeOrder sellOrder2 = createOrder(Type.SELL, new BigDecimal("49000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
////			TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account2);
////
////			// when
////			List<TradeHistoryResponse> responses = new ArrayList<>();
////			responses.addAll(orderBook.received(sellOrder1));
////			responses.addAll(orderBook.received(sellOrder2));
////			responses.addAll(orderBook.received(buyOrder));
////
////			// then
////			assertThat(responses).hasSize(2);
////			assertThat(responses.get(0).price()).isEqualTo(sellOrder1.getPrice());
////			assertThat(responses.get(1).quantity()).isEqualTo(new BigDecimal("5"));
////			assertThat(responses.get(1).price()).isEqualTo(sellOrder2.getPrice());
////		}
//
//		@Test
//		@DisplayName("TC8.1.5 부분 체결 테스트")
//		void testPartialOrderExecution() {
//			// Given
//			// 서로 다른 가격의 매도 주문 2개 추가
//			TradeOrder sellOrder1 = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(Type.SELL, new BigDecimal("50500"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//			orderBook.received(sellOrder1);
//			orderBook.received(sellOrder2);
//
//			// When
//			// 매수 주문 추가 (매도 주문보다 높은 가격, 매도 주문 총량보다 적은 수량)
//			TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("51000"), new BigDecimal("7"), OrderStatus.ACTIVE, account2);
//			orderBook.received(buyOrder);
//
//			// Then
//			// 호가창 확인
//			OrderBookResponse afterExecutionOrderBook = orderBook.getBook();
//
//			// 매도 호가 확인
//			List<PriceLevelDto> sellLevels = afterExecutionOrderBook.sellLevels();
//			assertEquals(1, sellLevels.size(), "매도 호가가 1개만 남아 있어야 함");
//			assertEquals(new BigDecimal("50500"), sellLevels.get(0).price(), "남은 매도 호가는 높은 가격의 호가여야 함");
//			assertEquals(new BigDecimal("8"), sellLevels.get(0).quantity(), "남은 매도 수량이 8이어야 함 (원래 10개에서 2개 체결)");
//
//			// 매수 호가 확인 - 모두 체결되어 없어야 함
//			List<PriceLevelDto> buyLevels = afterExecutionOrderBook.buyLevels();
//			assertEquals(0, buyLevels.size(), "매수 호가가 없어야 함 (모두 체결됨)");
//		}
//
//		@Test
//		@DisplayName("TC3.1.2 & TC3.2.2 부분 체결")
//		@Transactional
//		void testPartialExecution() throws MatchingException {
//			// given
//			TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account2);
//
//			// when
//			List<TradeHistoryResponse> responses = new ArrayList<>();
//			responses.addAll(orderBook.received(buyOrder));
//			responses.addAll(orderBook.received(sellOrder));
//
//			// then
//			OrderBookResponse response = orderBook.getBook();
//			assertFalse(response.buyLevels().isEmpty());
//			assertTrue(response.sellLevels().isEmpty());
//			assertThat(responses.get(0).quantity()).isEqualTo(new BigDecimal("5"));
//		}
//
//		@Test
//		@DisplayName("TC3.3 주문 불균형 상황 테스트")
//		@Transactional
//		void testOrderImbalance() {
//			// 다수의 매수 주문 생성
//			for (int i = 0; i < 5; i++) {
//				TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//				orderBook.received(buyOrder);
//			}
//
//			// 소수의 매도 주문 생성
//			TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account2);
//			orderBook.received(sellOrder);
//
//			OrderBookResponse response = orderBook.getBook();
//			assertFalse(response.buyLevels().isEmpty());
//			assertTrue(response.sellLevels().isEmpty());
//			assertEquals(new BigDecimal("45"), response.buyLevels().get(0).quantity());
//		}
//
//		@Test
//		@DisplayName("TC3.4 대량 주문 처리 테스트")
//		@Transactional
//		void testLargeOrderProcessing() throws MatchingException {
//			BigDecimal largeQuantity = new BigDecimal("1000000");
//			TradeOrder largeBuyOrder = createOrder(Type.BUY, new BigDecimal("50000"), largeQuantity, OrderStatus.ACTIVE, account1);
//			orderBook.received(largeBuyOrder);
//
//			TradeOrder smallSellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("100"),
//					OrderStatus.ACTIVE, account2);
//			orderBook.received(smallSellOrder);
//
//			OrderBookResponse response = orderBook.getBook();
//			assertFalse(response.buyLevels().isEmpty());
//			assertTrue(response.sellLevels().isEmpty());
//			assertEquals(largeQuantity.subtract(new BigDecimal("100")), response.buyLevels().get(0).quantity());
//		}
//	}
//
//	@Nested
//	@DisplayName("시장가 주문 테스트")
//	class MarketOrderTest {
//		@Test
//		@DisplayName("TC3.1.3 시장가 매수 주문 처리")
//		@Transactional
//		void testMarketBuyOrder() throws MatchingException {
//			TradeOrder sellOrder = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//			TradeOrder marketBuyOrder = createOrder(Type.BUY, BigDecimal.ZERO, new BigDecimal("10"), OrderStatus.MARKET, account2);
//
//			List<TradeHistoryResponse> responses = new ArrayList<>();
//			responses.addAll(orderBook.received(sellOrder));
//			responses.addAll(orderBook.received(marketBuyOrder));
//
//			assertThat(responses).hasSize(1);
//			assertThat(responses.get(0).buyOrderId()).isEqualTo(marketBuyOrder.getId());
//			assertThat(responses.get(0).sellOrderId()).isEqualTo(sellOrder.getId());
//			assertThat(responses.get(0).quantity()).isEqualTo(marketBuyOrder.getTotalQuantity());
//		}
//
//		@Test
//		@DisplayName("TC8.1.3 시장가 주문 테스트")
//		void testMarketOrder() {
//			// Given
//			OrderBook orderBookService = new OrderBook(COMPANY_CODE);
//
//			// 지정가 매도 주문 2개 추가 (서로 다른 가격)
//			TradeOrder sellOrder1 = createOrder(Type.SELL, new BigDecimal("50000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(Type.SELL, new BigDecimal("51000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			orderBookService.received(sellOrder1);
//			orderBookService.received(sellOrder2);
//
//			// When
//			// 시장가 매수 주문 추가 (가격은 0, 수량은 3)
//			TradeOrder marketBuyOrder = createOrder(Type.BUY, BigDecimal.ZERO, new BigDecimal("3"), OrderStatus.MARKET, account2);
//			orderBookService.received(marketBuyOrder);
//
//			// Then
//			// 호가창 확인
//			OrderBookResponse afterMarketOrderBook = orderBookService.getBook();
//			assertEquals(2, afterMarketOrderBook.sellLevels().size(), "매도 호가가 2개 있어야 함");
//			assertEquals(new BigDecimal("2"), afterMarketOrderBook.sellLevels().get(0).quantity(),
//					"첫 번째 매도 호가의 수량이 2로 줄어들어야 함");
//		}
//
//		@Test
//		@DisplayName("TC8.1.4 시장가 주문 체결 불가 테스트")
//		void testMarketOrderNoMatch() {
//			// given
//			TradeOrder marketBuyOrder = createOrder(Type.BUY, BigDecimal.ZERO, new BigDecimal("5"), OrderStatus.MARKET, account1);
//
//			// When & Then
//			// 예외가 발생해야 함
//			assertThrows(MatchingException.class, () -> orderBook.received(marketBuyOrder),
//					"매칭되는 매도 주문이 없을 때 MatchingException이 발생해야 함");
//
//			// 시장가 매도 주문 (매수 호가가 없음)
//			TradeOrder marketSellOrder = createOrder(Type.SELL, BigDecimal.ZERO, new BigDecimal("5"), OrderStatus.MARKET, account1);
//
//			// 예외가 발생해야 함
//			assertThrows(MatchingException.class, () -> orderBook.received(marketSellOrder),
//					"매칭되는 매수 주문이 없을 때 MatchingException이 발생해야 함");
//		}
//
//		@Test
//		@DisplayName("시장가 매수 주문시 가장 낮은 가격의 매도 주문부터 매칭된다.")
//		void matchBuyMarketOrderWithLowestPrice() {
//		    // given
//			TradeOrder sellOrder1 = createOrder(Type.SELL, new BigDecimal("40000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(Type.SELL, new BigDecimal("51000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder marketBuyOrder = createOrder(Type.BUY, BigDecimal.ZERO, new BigDecimal("10"), OrderStatus.MARKET, account2);
//
//		    // when
//			List<TradeHistoryResponse> responses = new ArrayList<>();
//			responses.addAll(orderBook.received(sellOrder1));
//			responses.addAll(orderBook.received(sellOrder2));
//			responses.addAll(orderBook.received(marketBuyOrder));
//
//		    // then
//			assertThat(responses).hasSize(2);
//			assertThat(responses.get(0).sellOrderId()).isEqualTo(sellOrder1.getId());
//			assertThat(responses.get(0).price()).isEqualTo(sellOrder1.getPrice());
//			assertThat(responses.get(1).sellOrderId()).isEqualTo(sellOrder1.getId());
//			assertThat(responses.get(1).price()).isEqualTo(sellOrder2.getPrice());
//		}
//
//		@Test
//		@DisplayName("시장가 매도 주문시 가장 높은 가격의 매수 주문부터 매칭된다.")
//		void matchSellMarketOrderWithHighestPrice() {
//			// given
//			TradeOrder buyOrder1 = createOrder(Type.BUY, new BigDecimal("40000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder2 = createOrder(Type.BUY, new BigDecimal("51000"), new BigDecimal("5"), OrderStatus.ACTIVE, account1);
//			TradeOrder marketSellOrder = createOrder(Type.SELL, BigDecimal.ZERO, new BigDecimal("10"), OrderStatus.MARKET, account2);
//
//			// when
//			List<TradeHistoryResponse> responses = new ArrayList<>();
//			responses.addAll(orderBook.received(buyOrder1));
//			responses.addAll(orderBook.received(buyOrder2));
//			responses.addAll(orderBook.received(marketSellOrder));
//
//			// then
//			assertThat(responses).hasSize(2);
//			assertThat(responses.get(0).sellOrderId()).isEqualTo(buyOrder2.getId());
//			assertThat(responses.get(0).price()).isEqualTo(buyOrder2.getPrice());
//			assertThat(responses.get(1).sellOrderId()).isEqualTo(buyOrder1.getId());
//			assertThat(responses.get(1).price()).isEqualTo(buyOrder1.getPrice());
//		}
//	}
//
//	@Test
//	@DisplayName("TC3.2.3 시장가 매도 주문 처리")
//	void testMarketSellOrder() throws MatchingException {
//		// given
//		TradeOrder buyOrder = createOrder(Type.BUY, new BigDecimal("50000"), new BigDecimal("10"), OrderStatus.ACTIVE, account1);
//		TradeOrder marketSellOrder = createOrder(Type.SELL, BigDecimal.ZERO, new BigDecimal("10"), OrderStatus.MARKET, account2);
//
//		// when
//		List<TradeHistoryResponse> responses = new ArrayList<>();
//		responses.addAll(orderBook.received(buyOrder));
//		responses.addAll(orderBook.received(marketSellOrder));
//
//		// then
//		assertThat(responses).hasSize(1);
//		assertThat(responses.get(0).buyOrderId()).isEqualTo(buyOrder.getId());
//		assertThat(responses.get(0).sellOrderId()).isEqualTo(marketSellOrder.getId());
//		assertThat(responses.get(0).quantity()).isEqualTo(marketSellOrder.getTotalQuantity());
//	}
//
//	@Nested
//	@DisplayName("주문 체결 순서 테스트")
//	class OrderMatchingSequenceTests {
//		@Test
//		@DisplayName("매수 주문의 경우 높은 가격의 주문부터 체결된다.")
//		void buyOrderHigherPricePriorityMatching() throws MatchingException {
//			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 1, 1);
//			TradeOrder buyOrder1 = createOrder(1L, Type.BUY, new BigDecimal(2000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder2 = createOrder(2L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder = createOrder(3L, Type.SELL, new BigDecimal(0), new BigDecimal(10), createdAt,
//					OrderStatus.MARKET, account2);
//
//			orderBook.received(buyOrder1);
//			orderBook.received(buyOrder2);
//			orderBook.received(sellOrder);
//
//			assertThat(buyOrder1.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//			assertThat(buyOrder2.getRemainingQuantity()).isEqualTo(new BigDecimal(10));
//			assertThat(sellOrder.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//		}
//
//		@Test
//		@DisplayName("매도 주문의 경우 낮은 가격의 주문부터 체결된다.")
//		void sellOrderLowerPricePriorityMatching() throws MatchingException {
//			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 1, 1);
//			TradeOrder sellOrder1 = createOrder(1L, Type.SELL, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(2L, Type.SELL, new BigDecimal(2000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder = createOrder(3L, Type.BUY, new BigDecimal(0), new BigDecimal(10), createdAt,
//					OrderStatus.MARKET, account2);
//
//			orderBook.received(sellOrder1);
//			orderBook.received(sellOrder2);
//			orderBook.received(buyOrder);
//
//			assertThat(sellOrder1.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//			assertThat(sellOrder2.getRemainingQuantity()).isEqualTo(new BigDecimal(10));
//			assertThat(buyOrder.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//		}
//
//		@Test
//		@DisplayName("매수 주문시, 같은 가격일 경우 먼저 주문이 들어온 주문부터 처리한다.")
//		void buyOrderTimePriorityMatching() throws MatchingException {
//			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 1, 1);
//			TradeOrder buyOrder1 = createOrder(1L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt.plusMinutes(1),
//					OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder2 = createOrder(2L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder = createOrder(3L, Type.SELL, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.MARKET, account2);
//			orderBook.received(buyOrder1);
//			orderBook.received(buyOrder2);
//			orderBook.received(sellOrder);
//
//			assertThat(buyOrder1.getRemainingQuantity()).isEqualTo(new BigDecimal(10));
//			assertThat(buyOrder2.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//			assertThat(sellOrder.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//		}
//
//		@Test
//		@DisplayName("매도 주문시, 같은 가격일 경우 먼저 주문이 들어온 주문부터 처리한다.")
//		void sellOrderTimePriorityMatching() throws MatchingException {
//			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 1, 1);
//			TradeOrder sellOrder1 = createOrder(1L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt.plusMinutes(1),
//					OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(2L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder = createOrder(3L, Type.SELL, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.MARKET, account2);
//
//			orderBook.received(sellOrder1);
//			orderBook.received(sellOrder2);
//			orderBook.received(buyOrder);
//
//			assertThat(sellOrder1.getRemainingQuantity()).isEqualTo(new BigDecimal(10));
//			assertThat(sellOrder2.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//			assertThat(buyOrder.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//		}
//
//		@Test
//		@DisplayName("매수 주문시, 모든 조건이 일치할 경우 수량이 많은 주문부터 체결한다.")
//		void buyOrderQuantityPriorityMatching() throws MatchingException {
//			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 1, 1);
//			TradeOrder buyOrder1 = createOrder(1L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder2 = createOrder(2L, Type.BUY, new BigDecimal(1000), new BigDecimal(11), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder = createOrder(3L, Type.SELL, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.MARKET, account2);
//
//			orderBook.received(buyOrder1);
//			orderBook.received(buyOrder2);
//			orderBook.received(sellOrder);
//
//			assertThat(buyOrder1.getRemainingQuantity()).isEqualTo(new BigDecimal(10));
//			assertThat(buyOrder2.getRemainingQuantity()).isEqualTo(new BigDecimal(1));
//			assertThat(sellOrder.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//		}
//
//		@Test
//		@DisplayName("매도 주문시, 모든 조건이 일치할 경우 수량이 많은 주문부터 체결한다.")
//		void sellOrderQuantityPriorityMatching() throws MatchingException {
//			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 1, 1);
//			TradeOrder sellOrder1 = createOrder(1L, Type.BUY, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder sellOrder2 = createOrder(2L, Type.BUY, new BigDecimal(1000), new BigDecimal(11), createdAt,
//					OrderStatus.ACTIVE, account1);
//			TradeOrder buyOrder = createOrder(3L, Type.SELL, new BigDecimal(1000), new BigDecimal(10), createdAt,
//					OrderStatus.MARKET, account2);
//
//			orderBook.received(sellOrder1);
//			orderBook.received(sellOrder2);
//			orderBook.received(buyOrder);
//
//			assertThat(sellOrder1.getRemainingQuantity()).isEqualTo(new BigDecimal(10));
//			assertThat(sellOrder2.getRemainingQuantity()).isEqualTo(new BigDecimal(1));
//			assertThat(buyOrder.getRemainingQuantity()).isEqualTo(BigDecimal.ZERO);
//		}
//	}
//
//	private TradeOrder createOrder(Type type, BigDecimal price, BigDecimal quantity, OrderStatus status, Account account) {
//		return TradeOrder.builder()
//				.companyCode(COMPANY_CODE)
//				.type(type)
//				.totalQuantity(quantity)
//				.remainingQuantity(quantity)
//				.status(status)
//				.price(price)
//				.account(account)
//				.createdDateTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
//				.build();
//	}
//
//	private TradeOrder createOrder(Long id, Type type, BigDecimal price, BigDecimal quantity, LocalDateTime createdAt,
//			OrderStatus status, Account account) {
//		return TradeOrder.builder()
//				.id(id)
//				.companyCode("005930")
//				.type(type)
//				.totalQuantity(quantity)
//				.remainingQuantity(quantity)
//				.status(OrderStatus.ACTIVE)
//				.price(price)
//				.status(status)
//				.account(account)
//				.createdDateTime(createdAt)
//				.build();
//	}
//}
//
