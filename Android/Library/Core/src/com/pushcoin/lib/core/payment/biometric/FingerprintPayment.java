package com.pushcoin.lib.core.payment.biometric;

import java.io.IOException;

import com.pushcoin.lib.core.data.IChallenge;
import com.pushcoin.lib.core.payment.IPayment;
import com.pushcoin.lib.core.utils.Logger;

public class FingerprintPayment implements IPayment {
	private static Logger log = Logger.getLogger(FingerprintPayment.class);

	public FingerprintPayment(byte[] data, int length) {
		log.d("data length: " + length);
	}

	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getMessage(IChallenge challenge) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
