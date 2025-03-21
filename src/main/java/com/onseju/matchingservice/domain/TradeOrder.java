package com.onseju.matchingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@AllArgsConstructor
@Builder
public class TradeOrder {

    private final Long id;

    private final String companyCode;

    private Type type;

    private OrderStatus status;

    private final BigDecimal totalQuantity;

    private AtomicReference<BigDecimal> remainingQuantity;

    private BigDecimal price;

    private LocalDateTime createdDateTime;

    private Long accountId;

    // 입력 주문과 같은 계정으로부터의 주문인지 확인
    public boolean isSameAccount(Long otherAccountId) {
        if (otherAccountId == null) {
            return false;
        }
        return otherAccountId.equals(this.accountId);
    }

    // 남은 양 감소
    public void decreaseRemainingQuantity(final BigDecimal quantity) {
        remainingQuantity.updateAndGet(before ->
                before.subtract(quantity).max(BigDecimal.ZERO)
        );
    }

    public boolean isSellType() {
        return type.isSell();
    }

    public BigDecimal calculateMatchQuantity(final TradeOrder other) {
        long min = Math.min(remainingQuantity.get().longValue(), other.getRemainingQuantity().get().longValue());
        return new BigDecimal(min);
    }

    // 체결 완료 여부 확인
    public void checkAndChangeOrderStatus() {
        if (this.remainingQuantity.get().equals(BigDecimal.ZERO)) {
            this.status = OrderStatus.COMPLETE;
        }
    }

    public boolean hasRemainingQuantity() {
        return !remainingQuantity.get().equals(BigDecimal.ZERO);
    }

    public boolean isMarketOrder() {
        return type.isMarket();
    }

    public void changeTypeToMarket() {
        if (isSellType()) {
            this.type = Type.MARKET_SELL;
            price = BigDecimal.ZERO;
            return;
        }
        this.type = Type.MARKET_BUY;
        price = BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TradeOrder that = (TradeOrder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}