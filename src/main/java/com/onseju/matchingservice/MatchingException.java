package com.onseju.matchingservice;

public class MatchingException extends RuntimeException {

	public MatchingException(String message) {
		super(message);
	}

	// 예외 던지기 -> tracking 과정을 없애 비용 절감
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
