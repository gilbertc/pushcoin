package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class Error implements Bundlable {
	public static final String KEY_REASON = "REASON";
	private Bundle bundle;

	public Error() {
		this(new Bundle());
	}

	public Error(Bundle bundle) {
		this.bundle = bundle;
	}

	public String getReason() {
		return this.bundle.getString(KEY_REASON);
	}

	public void setReason(String reason) {
		this.bundle.putString(KEY_REASON, reason);
	}

	public Bundle getBundle() {
		return bundle;
	}
}
