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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

public class TransactionKey {
	
	private static TransactionKey[] keys = null;
	public static void setKeys(TransactionKey[] newKeys) { keys = newKeys; }
	public static TransactionKey[] getKeys() { return keys; }

	public byte[] keyId;
	public String keyAttrs;
	public Date expire;
	public byte[] key;
	
	public IChallenge getChallenge()
	{
	    try
	    {
			long appSeed = Calendar.getInstance().getTimeInMillis();
			byte[] appSeedBytes = ByteBuffer.allocate(8).putLong(appSeed).array();
			
			byte[] combo = new byte[key.length + appSeedBytes.length]; 
			
			System.arraycopy(appSeedBytes, 0, combo, 0, appSeedBytes.length);
			System.arraycopy(key, 0, combo, appSeedBytes.length ,key.length);
			
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
	        crypt.update(combo);
	        
	        byte[] rawdigest = crypt.digest();
	        byte[] challengedata = new byte[8];
	        
	        System.arraycopy(rawdigest, 0, challengedata, 0, 8);
	        Challenge challenge = new Challenge(this, appSeedBytes, challengedata);
	        return challenge;
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        e.printStackTrace();
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
		
		return null;
	}
}
