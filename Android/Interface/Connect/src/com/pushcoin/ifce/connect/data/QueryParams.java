package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class QueryParams implements Bundlable {
	public static final String KEY_QUERY = "QUERY";

	private Bundle bundle;

	public QueryParams(Bundle bundle) {
		this.bundle = bundle;
	}

	public String getQuery() {
		return bundle.getString(KEY_QUERY, "");
	}

	public void setQuery(String query) {
		bundle.putString(KEY_QUERY, query);
	}

	public Bundle getBundle() {
		return bundle;
	}
}
