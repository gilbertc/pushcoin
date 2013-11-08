package com.pushcoin.core.payment.magnetic;

import java.io.IOException;

import com.pushcoin.core.data.Challenge;
import com.pushcoin.core.payment.IPayment;
import com.pushcoin.core.utils.Logger;

public class MagneticPayment implements IPayment {
	private static Logger log = Logger.getLogger(MagneticPayment.class);

	public MagneticPayment(byte[] data, int length) {
		log.d("data length: " + length);
	}

	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getMessage(Challenge challenge) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
