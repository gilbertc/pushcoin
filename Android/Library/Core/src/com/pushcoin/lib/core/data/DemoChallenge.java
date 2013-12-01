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
