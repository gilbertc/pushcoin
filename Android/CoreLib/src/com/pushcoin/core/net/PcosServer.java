package com.pushcoin.core.net;

import com.pushcoin.pcos.DocumentReader;
import com.pushcoin.pcos.InputDocument;
import com.pushcoin.pcos.OutputDocument;

public class PcosServer extends Server {

	public abstract class PcosResponseListener extends Server.ResponseListener {

		public abstract void onResponse(Object tag, InputDocument res);

		@Override
		public final void onResponse(Object tag, byte[] res) {

			try {
				this.onResponse(tag, new DocumentReader(res));
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
