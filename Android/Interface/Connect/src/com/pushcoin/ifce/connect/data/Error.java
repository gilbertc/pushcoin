package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class Error extends CallbackResult {
	public static final String KEY_REASON = "REASON";

	public Error() {
		this(new Bundle());
	}

	public Error(Bundle bundle) {
		super(bundle);
	}

	public String getReason() {
		return this.bundle.getString(KEY_REASON);
	}

	public void setReason(String reason) {
		this.bundle.putString(KEY_REASON, reason);
	}
}
