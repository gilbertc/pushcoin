package com.pushcoin.ifce.connect.listeners;

import com.pushcoin.ifce.connect.data.ChargeResult;

public interface ChargeResultListener {
	public void onResult(ChargeResult result);
	public void onResult(Error err);
}
