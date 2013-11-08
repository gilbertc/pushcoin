package com.pushcoin.core.payment;

import java.io.IOException;

import com.pushcoin.core.data.Challenge;

public interface IPayment {
	void connect() throws IOException;

	byte[] getMessage(Challenge challenge) throws Exception;

	void close() throws IOException;
}
