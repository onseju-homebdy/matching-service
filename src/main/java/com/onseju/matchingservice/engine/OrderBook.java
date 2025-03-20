package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.dto.TradeHistoryEvent;

import java.util.Collection;

/**
 * 시장에 존재하는 구매자와 판매자의 모든 주문에 대한 기록
 */
public interface OrderBook {

    Collection<TradeHistoryEvent> received(TradeOrder order);

    boolean isSellOrderBelowMarketPrice(TradeOrder order);

    boolean isBuyOrderAboveMarketPrice(TradeOrder order);
}
