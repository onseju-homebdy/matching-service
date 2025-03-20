package com.onseju.matchingservice.concurrent;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.dto.TradeHistoryEvent;
import com.onseju.matchingservice.engine.CompanyOrderBook;
import com.onseju.matchingservice.engine.OrderBook;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationConcurrentTest {

    private static final int THREAD_COUNT = 500;

    private final OrderBook orderBook = new CompanyOrderBook();


    @Test
    void shouldProcessConcurrentOrdersCorrectly() throws InterruptedException {

        List<TradeHistoryEvent> responses = new ArrayList<>(processOrders());

        verifyTradeResults(responses);
    }

    private List<TradeHistoryEvent> processOrders() throws InterruptedException {
        List<TradeOrder> sellOrders = createSellOrders();
        List<TradeHistoryEvent> historyEvents = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(sellOrders.size());

        for (TradeOrder order : sellOrders) {
            executorService.execute(() -> {
                try {
                    List<TradeHistoryEvent> result = orderBook.received(order);
                    historyEvents.addAll(result.stream().filter(Objects::nonNull).toList());
                } catch (Exception e) {
                    System.out.println("[Exception] " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        List<TradeOrder> buyOrders = createBuyOrders();
        for (TradeOrder order : buyOrders) {
            executorService.execute(() -> {
                try {
                    List<TradeHistoryEvent> result = orderBook.received(order);
                    historyEvents.addAll(result.stream().filter(Objects::nonNull).toList());
                } catch (Exception e) {
                    System.out.println("[Exception] " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        return historyEvents;
    }

    private void verifyTradeResults(List<TradeHistoryEvent> reponses) {
        assertThat(reponses).hasSize(THREAD_COUNT);
        verifyUniqueMatching(reponses);
    }

    private void verifyUniqueMatching(List<TradeHistoryEvent> reponses) {
        boolean[] ordersMatched = new boolean[THREAD_COUNT * 2 + 1];

        reponses.forEach(history -> {
            assertThat(ordersMatched[Math.toIntExact(history.sellOrderId())]).isFalse();
            assertThat(ordersMatched[Math.toIntExact(history.buyOrderId())]).isFalse();
            ordersMatched[Math.toIntExact(history.sellOrderId())] = true;
            ordersMatched[Math.toIntExact(history.buyOrderId())] = true;
        });
    }

    private List<TradeOrder> createSellOrders() {
        List<TradeOrder> orders = new ArrayList<>();
        for (int i = 1; i < THREAD_COUNT + 1; i++) {
            orders.add(createOrder((long) i, Type.LIMIT_SELL, new BigDecimal(1000), BigDecimal.ONE, (long) i));
        }
        return orders;
    }

    private List<TradeOrder> createBuyOrders() {
        List<TradeOrder> orders = new ArrayList<>();
        for (int i = THREAD_COUNT + 1; i < THREAD_COUNT * 2 + 1; i++) {
            orders.add(createOrder((long) i, Type.LIMIT_BUY, new BigDecimal(1000), BigDecimal.ONE, (long) i));
        }
        return orders;
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