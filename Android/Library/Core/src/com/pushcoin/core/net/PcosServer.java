package com.pushcoin.core.net;

import com.pushcoin.core.data.Messages;
import com.pushcoin.pcos.DocumentReader;
import com.pushcoin.pcos.InputBlock;
import com.pushcoin.pcos.InputDocument;
import com.pushcoin.pcos.OutputDocument;
import com.pushcoin.pcos.PcosError;

public class PcosServer extends Server {

	public abstract class PcosResponseListener extends Server.ResponseListener {

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

	public InputDocument post(String url, OutputDocument doc) throws Exception {
		return this.post(null, url, doc);
	}

	public InputDocument post(Object tag, String url, OutputDocument doc)
			throws Exception {
		return new DocumentReader(post(tag, url, doc.toBytes()));
	}

	public void postAsync(String url, OutputDocument doc,
			PcosResponseListener listener) {
		this.postAsync(null, url, doc, listener);
	}

	public void postAsync(Object tag, String url, OutputDocument doc,
			PcosResponseListener listener) {

		try {
			this.postAsync(tag, url, doc.toBytes(), listener);
		} catch (Exception ex) {
			listener.onError(tag, ex);
		}
	}

}
