package com.pushcoin.icebreaker;

import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;
import java.io.UnsupportedEncodingException;

class PcosHelper
{
	static public class ErrorInfo
	{
		String txnId;
		long errorCode;
		String message;
	}

	static ErrorInfo parseError(InputDocument doc) throws PcosError
	{
		try 
		{
			if ( doc.getDocumentName().equals( Conf.PCOS_DOC_ERROR) )
			{
				InputBlock bo = doc.getBlock("Bo");	
				ErrorInfo res = new ErrorInfo();
				res.txnId = new String(bo.readByteStr(Conf.PCOS_MAXLEN_TXN_ID), "UTF-8");
				res.errorCode = bo.readUint();
				res.message = bo.readString(Conf.PCOS_MAXLEN_ERROR_MESSAGE);
				return res;
			}
		} catch (UnsupportedEncodingException e) {}
		throw new PcosError(PcosErrorCode.ERR_MALFORMED_MESSAGE, "Cannot parse " + doc.getDocumentName());
	}

	static public class RegisterAckResult
	{
		byte[] mat;
	}

	static RegisterAckResult parseRegisterAck(InputDocument doc) throws PcosError
	{
		if ( doc.getDocumentName().equals( Conf.PCOS_DOC_REGISTER_ACK) )
		{
			InputBlock bo = doc.getBlock("Bo");	
			RegisterAckResult res = new RegisterAckResult();
			res.mat = bo.readByteStr(Conf.PCOS_MAXLEN_TXN_ID);
			return res;
		}
		throw new PcosError(PcosErrorCode.ERR_MALFORMED_MESSAGE, "Cannot parse " + doc.getDocumentName());
	}
}
