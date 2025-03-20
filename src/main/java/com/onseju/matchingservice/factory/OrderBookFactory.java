package com.onseju.matchingservice.factory;

import com.onseju.matchingservice.engine.CompanyOrderBook;
import com.onseju.matchingservice.engine.OrderBook;
import org.springframework.stereotype.Component;

@Component
public class OrderBookFactory {

    public OrderBook createOrderBook() {
        return new CompanyOrderBook();
    }
}
