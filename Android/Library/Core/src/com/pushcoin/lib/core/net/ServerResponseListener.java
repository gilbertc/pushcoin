package com.pushcoin.lib.core.net;

public abstract class ServerResponseListener {
	public abstract void onError(Object tag, Exception ex);

	public abstract void onResponse(Object tag, byte[] res);

	public void onFinished(Object tag) {
	}
}