package com.pushcoin.core.payment.nfc.protocols;

import java.io.IOException;

import com.pushcoin.pcos.OutputBlock;

import com.pushcoin.core.data.Challenge;

public abstract class NfcProtocol {
	
	public static final int PROTO_NXP_NTAG203 = 0;
	public static final int PROTO_VERAYO_M1HW_50PF = 1;
	
	public abstract OutputBlock buildPcosBlock(Challenge challenge) throws IOException;
	
}