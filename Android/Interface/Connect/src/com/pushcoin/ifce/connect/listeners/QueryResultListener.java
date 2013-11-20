package com.pushcoin.ifce.connect.listeners;

import com.pushcoin.ifce.connect.data.QueryResult;

public interface QueryResultListener {
	public void onResult(QueryResult result);
	public void onResult(Error err);
}
