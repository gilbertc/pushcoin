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

package com.pushcoin.lib.core.payment.nfc;

import java.io.IOException;


import com.pushcoin.lib.core.data.IChallenge;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.pcos.BlockWriter;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputBlock;
import com.pushcoin.lib.pcos.PcosError;

public class NfcPayload {
	private static Logger log = Logger.getLogger(NfcPayload.class);

	String deviceId;
	public String getDeviceId()
	{ 
		return deviceId;
	}
	
	public NfcPayload(InputDocument doc) throws PcosError
	{
		InputBlock blk = doc.getBlock("P1");
		deviceId = blk.readString(20);
	}
	
	public OutputBlock buildPcosBlock(IChallenge challenge) throws IOException
	{
		OutputBlock ret = new BlockWriter("P1");
		try {
			ret.writeString(deviceId);
		}
		catch (PcosError e)
		{
			log.d("PcosError", e);
		}
		return ret;
	}

}
