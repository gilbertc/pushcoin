package com.pushcoin.lib.core.payment.nfc;

import java.io.IOException;


import com.pushcoin.lib.core.data.Challenge;
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
	
	public OutputBlock buildPcosBlock(Challenge challenge) throws IOException
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
