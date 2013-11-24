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
