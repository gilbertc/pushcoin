package com.pushcoin.lib.core.payment;

import java.io.IOException;

import com.pushcoin.lib.core.data.Challenge;

public interface IPayment {
	void connect() throws IOException;

	byte[] getMessage(Challenge challenge) throws Exception;

	void close() throws IOException;
}
