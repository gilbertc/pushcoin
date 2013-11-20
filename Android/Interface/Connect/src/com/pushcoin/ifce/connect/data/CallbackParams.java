package com.pushcoin.ifce.connect.data;

import android.os.Bundle;
import android.os.Messenger;

public class CallbackParams implements Bundlable {
	public static final String KEY_MESSENGER = "MESSENGER";

	protected Bundle bundle;

	public CallbackParams(Bundle bundle) {
		this.bundle = bundle;
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
}
