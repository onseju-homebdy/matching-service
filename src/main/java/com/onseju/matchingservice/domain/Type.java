package com.onseju.matchingservice.domain;

public enum Type {

	MARKET_SELL,
	MARKET_BUY,
	LIMIT_SELL,
	LIMIT_BUY;

	public boolean isMarket() {
		return this == MARKET_SELL || this == MARKET_BUY;
	}

	public boolean isSell() {
		return this == MARKET_SELL || this == LIMIT_SELL;
	}
}
