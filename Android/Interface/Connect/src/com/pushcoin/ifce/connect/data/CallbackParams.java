package com.pushcoin.ifce.connect.data;

import android.os.Bundle;
import android.os.Messenger;

public class CallbackParams implements Bundlable {
	public static final String KEY_MESSENGER = "MESSENGER";
	public static final String KEY_CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";

	protected Bundle bundle;

	public CallbackParams(Bundle bundle) {
		this.bundle = bundle;
		this.bundle.setClassLoader(Thread.currentThread()
				.getContextClassLoader());
	}

	public Messenger getMessenger() {
		return bundle.getParcelable(KEY_MESSENGER);
	}

	public void setMessenger(Messenger value) {
		bundle.putParcelable(KEY_MESSENGER, value);
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