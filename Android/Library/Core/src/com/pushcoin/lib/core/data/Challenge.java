/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.lib.core.data;

import java.util.Date;

public class Challenge implements IChallenge{
	
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
