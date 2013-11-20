package com.pushcoin.ifce.connect.data;

import java.util.ArrayList;

import android.os.Bundle;

public class QueryResult implements Bundlable {
	public static final String KEY_QUERY = "QUERY";
	public static final String KEY_CUSTOMERS = "CUSTOMERS";

	private Bundle bundle;

	public QueryResult() {
		this(new Bundle());
	}

	public QueryResult(Bundle bundle) {
		this.bundle = bundle;
		this.bundle.setClassLoader(Thread.currentThread().getContextClassLoader());
	}

	public Bundle getBundle() {
		return bundle;
	}

	public String getQuery() {
		return bundle.getString(KEY_QUERY, "");
	}

	public void setQuery(String query) {
		bundle.putString(KEY_QUERY, query);
	}

	public ArrayList<Customer> getCustomers() {
		return bundle.getParcelableArrayList(KEY_CUSTOMERS);
	}

	public void setCustomers(ArrayList<Customer> values) {
		bundle.putParcelableArrayList(KEY_CUSTOMERS, values);
	}

}
