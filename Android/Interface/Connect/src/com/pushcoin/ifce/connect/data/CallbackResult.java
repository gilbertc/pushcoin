package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class CallbackResult implements Bundlable {
	public static final String KEY_CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";

	protected Bundle bundle;

	public CallbackResult(Bundle bundle) {
		this.bundle = bundle;
		this.bundle.setClassLoader(Thread.currentThread()
				.getContextClassLoader());
	}

	public Bundle getBundle() {
		return bundle;
	}

	public String getClientRequestId() {
		return bundle.getString(KEY_CLIENT_REQUEST_ID, "");
	}

	public void setClientRequestId(String value) {
		bundle.putString(KEY_CLIENT_REQUEST_ID, value);
	}

}
