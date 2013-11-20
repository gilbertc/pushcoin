package com.pushcoin.lib.core.data;

import java.util.Date;

public interface IChallenge {
	public byte[] getKeyId();
	
	public byte[] getAppSeed();
	
	public byte[] getChallenege();
	
	public Date getExpire();
	
}
