package com.pushcoin.core.interfaces;

import android.app.Activity;

public enum Results {
	OK(Activity.RESULT_OK), CANCELED(Activity.RESULT_CANCELED), ERROR(Activity.RESULT_FIRST_USER + 1);

	private final int value;

	private Results(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
