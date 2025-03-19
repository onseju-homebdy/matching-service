package com.onseju.matchingservice.dto;

import java.math.BigDecimal;

public record TradeHistoryEvent(
        String companyCode,
        Long buyOrderId,
        Long sellOrderId,
        BigDecimal quantity,
        BigDecimal price,
        Long tradeAt
) {
}
