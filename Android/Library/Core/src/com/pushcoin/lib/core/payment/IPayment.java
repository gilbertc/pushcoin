package com.pushcoin.lib.core.payment;

import java.io.IOException;

import com.pushcoin.lib.core.data.IChallenge;

public interface IPayment {
	void connect() throws IOException;

	byte[] getMessage(IChallenge challenge) throws Exception;

	void close() throws IOException;
}
