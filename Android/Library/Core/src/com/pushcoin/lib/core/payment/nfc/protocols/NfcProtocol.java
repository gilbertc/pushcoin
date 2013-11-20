package com.pushcoin.lib.core.payment.nfc.protocols;

import java.io.IOException;

import com.pushcoin.lib.pcos.OutputBlock;

import com.pushcoin.lib.core.data.IChallenge;

public abstract class NfcProtocol {
	
	public static final int PROTO_NXP_NTAG203 = 0;
	public static final int PROTO_VERAYO_M1HW_50PF = 1;
	
	public abstract OutputBlock buildPcosBlock(IChallenge challenge) throws IOException;
	
}
