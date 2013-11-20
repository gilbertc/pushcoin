package com.pushcoin.ifce.connect.listeners;

import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.ifce.connect.data.Error;

public interface QueryResultListener {
	public void onResult(QueryResult result);
	public void onResult(Error err);
}
