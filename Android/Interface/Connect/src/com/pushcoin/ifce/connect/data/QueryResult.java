/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
