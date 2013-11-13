package com.pushcoin.lib.core.net;

import com.pushcoin.lib.core.data.Messages;
import com.pushcoin.lib.pcos.DocumentReader;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputDocument;
import com.pushcoin.lib.pcos.PcosError;

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
	
	public InputDocument stage(OutputDocument doc) throws Exception {
		return this.stage(null, doc);
	}

	public InputDocument stage(Object tag, OutputDocument doc)
			throws Exception {
		return new DocumentReader(stage(tag, doc.toBytes()));
	}

	public void stageAsync(OutputDocument doc,
			PcosResponseListener listener) {
		this.stageAsync(null, doc, listener);
	}

	public void stageAsync(Object tag, OutputDocument doc,
			PcosResponseListener listener) {

		try {
			this.stageAsync(tag, doc.toBytes(), listener);
		} catch (Exception ex) {
			listener.onError(tag, ex);
		}
	}

}
