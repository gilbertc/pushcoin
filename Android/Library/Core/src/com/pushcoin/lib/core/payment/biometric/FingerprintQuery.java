package com.pushcoin.lib.core.payment.biometric;

import com.pushcoin.lib.core.query.IQuery;
import com.pushcoin.lib.core.utils.Logger;

public class FingerprintQuery implements IQuery {
	private static Logger log = Logger.getLogger(FingerprintQuery.class);

	public FingerprintQuery(byte[] data, int length) {
		log.d("data length: " + length);
	}

	@Override
	public byte[] getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
