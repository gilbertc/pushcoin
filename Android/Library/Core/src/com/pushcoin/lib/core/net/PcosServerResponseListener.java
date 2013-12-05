package com.pushcoin.lib.core.net;

import com.pushcoin.lib.core.data.Messages;
import com.pushcoin.lib.pcos.DocumentReader;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.PcosError;

public abstract class PcosServerResponseListener extends ServerResponseListener {

	public void onErrorResponse(Object tag, byte[] trxId, long ec,
			String reason) {
	}

	public void onSuccessResponse(Object tag, byte[] refData, byte[] trxId) {
	}

	public void onResponse(Object tag, InputDocument res) throws PcosError {
	}

	@Override
	public final void onResponse(Object tag, byte[] data) {
		try {
			DocumentReader res = new DocumentReader(data);
			if (res.getDocumentName().contains(Messages.MSG_ERROR)) {
				InputBlock bo = res.getBlock("Bo");
				byte[] trxId = bo.readByteStr(0);
				long ec = bo.readUint();
				String reason = bo.readString(0);

				onErrorResponse(tag, trxId, ec, reason);
			} else if (res.getDocumentName().contains(Messages.MSG_SUCCESS)) {
				InputBlock bo = res.getBlock("Bo");
				byte[] refData = bo.readByteStr(0);
				byte[] trxId = bo.readByteStr(0);

				onSuccessResponse(tag, refData, trxId);
			} else {
				this.onResponse(tag, res);
			}

		} catch (Exception ex) {
			this.onError(tag, ex);
		}
	}
}