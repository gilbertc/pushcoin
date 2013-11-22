package com.pushcoin.ifce.connect.listeners;

import com.pushcoin.ifce.connect.data.Cancelled;

public interface PollResultListener extends QueryResultListener,
		ChargeResultListener {
	public void onResult(Cancelled cancelled);
}
