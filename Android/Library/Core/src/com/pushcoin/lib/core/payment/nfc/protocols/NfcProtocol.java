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

package com.pushcoin.lib.core.payment.nfc.protocols;

import java.io.IOException;

import com.pushcoin.lib.pcos.OutputBlock;

import com.pushcoin.lib.core.data.IChallenge;

public abstract class NfcProtocol {
	
	public static final int PROTO_NXP_NTAG203 = 0;
	public static final int PROTO_VERAYO_M1HW_50PF = 1;
	
	public abstract OutputBlock buildPcosBlock(IChallenge challenge) throws IOException;
	
}
