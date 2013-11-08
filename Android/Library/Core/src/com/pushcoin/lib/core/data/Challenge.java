package com.pushcoin.lib.core.data;

import java.util.Date;

public class Challenge {
	
	private TransactionKey key;
	private byte[] appSeed;
	private byte[] challenge;
	
	public Challenge(TransactionKey key, byte[] appSeed, byte[] challenge)
	{
		this.key = key;
		this.appSeed = appSeed;
		this.challenge = challenge;
	}
	
	public byte[] getKeyId()
	{ 
		return this.key.keyId;
	}
	
	public byte[] getAppSeed()
	{
		return this.appSeed;
	}
	
	public byte[] getChallenege()
	{
		return this.challenge;
	}
	
	public Date getExpire()
	{
		return this.key.expire;
	}
	
}
