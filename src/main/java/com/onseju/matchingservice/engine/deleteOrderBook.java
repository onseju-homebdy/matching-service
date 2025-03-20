//package com.onseju.matchingservice.engine;
//
//import com.onseju.matchingservice.domain.OrderStatus;
//import com.onseju.matchingservice.domain.TradeOrder;
//import com.onseju.matchingservice.domain.Type;
//import com.onseju.matchingservice.dto.TradeHistoryEvent;
//import com.onseju.matchingservice.exception.MatchingException;
//import lombok.RequiredArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentSkipListMap;
//import java.util.concurrent.ConcurrentSkipListSet;
//import java.util.concurrent.locks.ReentrantLock;
//
//@RequiredArgsConstructor
//public class OrderBook {
//
//    // 종목 번호
//    private final String companyCode;
//    // 매도 주문: 낮은 가격 우선
//    private final ConcurrentSkipListMap<BigDecimal, ConcurrentSkipListSet<TradeOrder>> sellOrders = new ConcurrentSkipListMap<>();
//    // 매수 주문: 높은 가격 우선
//    private final ConcurrentSkipListMap<BigDecimal, ConcurrentSkipListSet<TradeOrder>> buyOrders = new ConcurrentSkipListMap<>(
//            Collections.reverseOrder());
//
//    private final ReentrantLock matchlock = new ReentrantLock();
//
//    /**
//     * 주문 접수 및 처리
//     */
//    public void received(final TradeOrder tradeOrder) {
//        if (tradeOrder.getStatus() == OrderStatus.MARKET) {
//            processMarketOrder(tradeOrder);
//        } else {
//            processLimitOrder(tradeOrder);
//        }
//    }
//
//    /**
//     * 시장가 주문 처리
//     */
//    private void processMarketOrder(final TradeOrder tradeOrder) {
//        if (tradeOrder.getType() == Type.BUY) {
//            matchMarketBuyOrder(tradeOrder);
//        } else {
//            matchMarketSellOrder(tradeOrder);
//        }
//    }
//
//    /**
//     * 지정가 주문 처리
//     */
//    private void processLimitOrder(final TradeOrder tradeOrder) {
//        if (tradeOrder.getType() == Type.BUY) {
//            matchBuyOrder(tradeOrder);
//        } else {
//            matchSellOrder(tradeOrder);
//        }
//    }
//
//    /**
//     * 지정가 매도 주문 처리
//     */
//    private void matchSellOrder(final TradeOrder sellOrder) {
//        while (sellOrder.hasRemainingQuantity()) {
//            // 매도가보다 높거나 같은 매수 주문 찾기
//            Map.Entry<BigDecimal, ConcurrentSkipListSet<TradeOrder>> bestBuy = buyOrders.firstEntry();
//
//            if (bestBuy == null || bestBuy.getKey().compareTo(sellOrder.getPrice()) < 0) {
//                // 매칭되는 매수 주문이 없으면 주문장에 추가
//                addToOrderBook(sellOrders, sellOrder);
//                break;
//            }
//
//            // 주문 매칭 처리
//            matchlock.lock();
//            matchOrders(bestBuy.getValue(), sellOrder);
//            matchlock.unlock();
//
//            // 매수 큐가 비었으면 제거
//            if (bestBuy.getValue().isEmpty()) {
//                buyOrders.remove(bestBuy.getKey());
//            }
//        }
//    }
//
//    /**
//     * 시장가 매도 주문 처리
//     */
//    private void matchMarketSellOrder(final TradeOrder sellOrder) {
//        while (sellOrder.hasRemainingQuantity()) {
//            // 매수 주문 찾기
//            Map.Entry<BigDecimal, ConcurrentSkipListSet<TradeOrder>> bestBuy = buyOrders.firstEntry();
//            if (bestBuy == null) {
//                throw new MatchingException("주문 체결 불가 : " + sellOrder.getRemainingQuantity());
//            }
//
//            // 주문 매칭 처리
//            matchlock.lock();
//            matchOrders(bestBuy.getValue(), sellOrder);
//            matchlock.unlock();
//
//            // 매수 큐가 비었으면 제거
//            if (bestBuy.getValue().isEmpty()) {
//                buyOrders.remove(bestBuy.getKey());
//            }
//        }
//    }
//
//    /**
//     * 지정가 매수 주문 처리
//     */
//    private void matchBuyOrder(final TradeOrder buyOrder) {
//        while (buyOrder.hasRemainingQuantity()) {
//            // 매수가보다 낮거나 같은 매도 주문 찾기
//            Map.Entry<BigDecimal, ConcurrentSkipListSet<TradeOrder>> bestSell = sellOrders.firstEntry();
//
//            if (bestSell == null || bestSell.getKey().compareTo(buyOrder.getPrice()) > 0) {
//                addToOrderBook(buyOrders, buyOrder);
//                break;
//            }
//
//            // 주문 매칭 처리
//            matchlock.lock();
//            matchOrders(bestSell.getValue(), buyOrder);
//            matchlock.unlock();
//
//            // 매도 큐가 비었으면 제거
//            if (bestSell.getValue().isEmpty()) {
//                sellOrders.remove(bestSell.getKey());
//            }
//        }
//    }
//
//    /**
//     * 시장가 매수 주문 처리
//     */
//    private void matchMarketBuyOrder(final TradeOrder buyOrder) {
//        while (buyOrder.hasRemainingQuantity()) {
//            // 매도 주문 찾기
//            Map.Entry<BigDecimal, ConcurrentSkipListSet<TradeOrder>> bestSell = sellOrders.firstEntry();
//
//            if (bestSell == null) {
//                throw new MatchingException("주문 체결 불가 : " + buyOrder.getRemainingQuantity());
//            }
//
//            // 주문 매칭 처리
//            matchlock.lock();
//            matchOrders(bestSell.getValue(), buyOrder);
//            matchlock.unlock();
//
//            // 매도 큐가 비었으면 제거
//            if (bestSell.getValue().isEmpty()) {
//                sellOrders.remove(bestSell.getKey());
//            }
//        }
//    }
//
//    /**
//     * 주문 매칭 처리 - 상태 및 수량 변경 후 DB 업데이트 로직 추가
//     */
//    private void matchOrders(final ConcurrentSkipListSet<TradeOrder> existingOrders, final TradeOrder incomingOrder) {
//        // 처리 중에 제외된 주문들을 임시 저장
//        final ConcurrentSkipListSet<TradeOrder> skippedOrders = new ConcurrentSkipListSet<>(
//                Comparator.comparing(TradeOrder::getCreatedDateTime)
//                        .thenComparing(TradeOrder::getTotalQuantity, Comparator.reverseOrder())
//                        .thenComparing(TradeOrder::getId)
//        );
//
//        // 변경된 주문을 추적하기 위한 Set
//        final Set<TradeOrder> orderToUpdate = ConcurrentHashMap.newKeySet();
//
//        while (!existingOrders.isEmpty() && incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
//            // 가장 우선순위가 높은 주문 가져오기 (첫 번째 요소)
//            final TradeOrder existingOrder = existingOrders.first();
//            // Set에서 제거
//            existingOrders.remove(existingOrder);
//
//            // 동일 유저 주문인 경우
//            if (incomingOrder.hasSameAccount(existingOrder.getAccountId())) {
//                // 임시 큐에 저장
//                skippedOrders.add(existingOrder);
//                continue;
//            }
//
//            final BigDecimal matchedQuantity = incomingOrder.calculateMatchQuantity(existingOrder);
//            final BigDecimal matchPrice = existingOrder.getPrice(); // 체결 가격은 항상 기존 주문 가격
//
//            // 0. 매칭 전 값들 기록
//            final BigDecimal originalIncomingRemaining = incomingOrder.getRemainingQuantity().get();
//            final BigDecimal originalExistingRemaining = existingOrder.getRemainingQuantity().get();
//            final OrderStatus originalIncomingStatus = incomingOrder.getStatus();
//            final OrderStatus originalExistingStatus = existingOrder.getStatus();
//
//            // 1. 주문 수량 업데이트
////            incomingOrder.processMatch(matchedQuantity);
////            existingOrder.processMatch(matchedQuantity);
//
//            // 2. 상태나 남은 수량이 변경된 경우 업데이트할 주문 목록에 추가
//            if (originalIncomingStatus != incomingOrder.getStatus()
//                    || originalIncomingRemaining.compareTo(incomingOrder.getRemainingQuantity()) != 0) {
//                orderToUpdate.add(incomingOrder);
//            }
//
//            if (originalExistingStatus != existingOrder.getStatus()
//                    || originalExistingRemaining.compareTo(existingOrder.getRemainingQuantity()) != 0) {
//                orderToUpdate.add(existingOrder);
//            }
//
//            // 3. 매수자/매도자 결정
//            TradeOrder buyOrder, sellOrder;
//            if (incomingOrder.isSellType()) {
//                buyOrder = existingOrder;
//                sellOrder = incomingOrder;
//            } else {
//                buyOrder = incomingOrder;
//                sellOrder = existingOrder;
//            }
//
//            // 4. 거래 처리
//            processTradeMatch(buyOrder, sellOrder, matchPrice, matchedQuantity);
//
//            // 5. 완전 체결되지 않은 주문은 다시 큐에 추가
//            if (existingOrder.isCompletelyFilled()) {
//                existingOrders.add(existingOrder);
//            }
//        }
//
//        // 6. 임시 큐에 저장했던 건너뛴 주문들을 다시 원래 큐에 추가
//        while (!skippedOrders.isEmpty()) {
//            final TradeOrder skippedOrder = skippedOrders.first();
//            skippedOrders.remove(skippedOrder);
//            existingOrders.add(skippedOrder);
//        }
//
//        // 7. 인커밍 주문의 남은 수량 처리
//        if (incomingOrder.isCompletelyFilled()) {
//            // 주문 타입에 맞는 주문장에 남은 수량이 있는 주문 추가
//            if (incomingOrder.getType() == Type.BUY) {
//                addToOrderBook(buyOrders, incomingOrder);
//            } else {
//                addToOrderBook(sellOrders, incomingOrder);
//            }
//        }
//
//    }
//
//    // 매수/매도 주문 체결 처리
//    private TradeHistoryEvent createTradeHistoryEvent(
//            final TradeOrder incomingOrder,
//            final TradeOrder existingOrder,
//            final BigDecimal price,
//            final BigDecimal quantity
//    ) {
//        return createTradeHistoryEvent(incomingOrder, existingOrder, price, quantity);
//    }
//
//    // 매도자, 매수자 구분 후 응답 생성
//    private TradeHistoryEvent createResponse(final TradeOrder incomingOrder, final TradeOrder foundOrder, BigDecimal matchedQuantity, BigDecimal matchPrice) {
//        if (incomingOrder.isSellType()) {
//            return new TradeHistoryEvent(
//                    incomingOrder.getCompanyCode(),
//                    foundOrder.getId(),
//                    incomingOrder.getId(),
//                    matchedQuantity,
//                    incomingOrder.getPrice(),
//                    Instant.now().getEpochSecond()
//            );
//        }
//        return new TradeHistoryEvent(
//                incomingOrder.getCompanyCode(),
//                incomingOrder.getId(),
//                foundOrder.getId(),
//                matchedQuantity,
//                incomingOrder.getPrice(),
//                Instant.now().getEpochSecond()
//        );
//    }
//
//    /**
//     * 주문장에 주문 추가
//     */
//    private void addToOrderBook(final ConcurrentSkipListMap<BigDecimal, ConcurrentSkipListSet<TradeOrder>> orderBook,
//                                final TradeOrder tradeOrder) {
//        if (tradeOrder.getPrice().compareTo(BigDecimal.ZERO) == 0) {
//            return;
//        }
//
//        orderBook.computeIfAbsent(
//                tradeOrder.getPrice(),
//                k -> new ConcurrentSkipListSet<>(
//                        Comparator.comparing(TradeOrder::getCreatedDateTime)
//                                .thenComparing(TradeOrder::getTotalQuantity, Comparator.reverseOrder())
//                                .thenComparing(TradeOrder::getId) // 중복 방지를 위한 추가 비교자
//                )
//        ).add(tradeOrder);
//    }
//}
