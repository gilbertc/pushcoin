package com.pushcoin.lib.core.data;

import java.util.Calendar;
import java.util.Date;

public class DemoChallenge implements IChallenge {

	Date expire;
	byte[] keyId;
	byte[] appSeed;
	byte[] challenge;

	public DemoChallenge() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1);

		expire = c.getTime();
		keyId = new byte[] { 0, 1, 2, 3, 4 };
		appSeed = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		challenge = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4,
				5, 6, 7, 8, 9 };
	}

	public byte[] getKeyId() {
		return keyId;
	}

	public byte[] getAppSeed() {
		return appSeed;
	}

	public byte[] getChallenege() {
		return challenge;
	}

	public Date getExpire() {
		return expire;
	}

}
