package com.pushcoin.ifce.connect.data;

import java.util.ArrayList;

import android.os.Bundle;

public class QueryResult extends CallbackResult {
	public static final String KEY_QUERY = "QUERY";
	public static final String KEY_CUSTOMERS = "CUSTOMERS";

	public QueryResult() {
		this(new Bundle());
	}

	public QueryResult(Bundle bundle) {
		super(bundle);
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
