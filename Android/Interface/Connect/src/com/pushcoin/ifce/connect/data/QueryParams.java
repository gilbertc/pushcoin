package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class QueryParams extends CallbackParams {
	public static final String KEY_QUERY = "QUERY";

	public QueryParams() {
		this(new Bundle());
	}

	public QueryParams(Bundle bundle) {
		super(bundle);
	}

	public String getQuery() {
		return bundle.getString(KEY_QUERY, "");
	}

	public void setQuery(String query) {
		bundle.putString(KEY_QUERY, query);
	}

}
