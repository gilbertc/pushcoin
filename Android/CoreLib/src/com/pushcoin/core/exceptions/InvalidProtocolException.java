package com.pushcoin.core.exceptions;

public class InvalidProtocolException extends RecoverableException {
	private static final long serialVersionUID = 4249014083718566948L;

	public InvalidProtocolException(String message) {
		super(message);
	}
}