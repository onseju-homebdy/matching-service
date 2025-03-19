package com.onseju.matchingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TradeOrder {

    private final Long id;

    private final String companyCode;

    private final Type type;

    private OrderStatus status;

    private final BigDecimal totalQuantity;

    private BigDecimal remainingQuantity;

    private BigDecimal price;

    private LocalDateTime createdDateTime;

    private Long accountId;

    // 입력 주문과 같은 계정으로부터의 주문인지 확인
    public boolean hasSameAccount(Long otherAccountId) {
        if (otherAccountId == null) {
            return false;
        }
        return otherAccountId.equals(this.accountId);
    }

    // 남은 양 감소
    public void decreaseRemainingQuantity(final BigDecimal quantity) {
        this.remainingQuantity = this.remainingQuantity.subtract(quantity);
        if (this.remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            this.status = OrderStatus.COMPLETE;
        }
    }

    public boolean isSellType() {
        return type == Type.SELL;
    }

    public BigDecimal calculateMatchQuantity(final TradeOrder other) {
        return remainingQuantity.min(other.getRemainingQuantity());
    }

    // 체결 완료 여부 확인
    public boolean isCompletelyFilled() {
        return this.remainingQuantity.compareTo(BigDecimal.ZERO) == 0;
    }
}