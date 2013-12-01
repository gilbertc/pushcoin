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

package com.pushcoin.srv.gateway.demo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.pushcoin.ifce.connect.data.Amount;
import com.pushcoin.ifce.connect.data.Customer;
import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.srv.gateway.R;

public class QueryResultBuilder {
	public static QueryResult makeResult(Context context, String query) {

		QueryResult res = new QueryResult();
		res.setQuery(query);

		// fabricate date of balance
		long balanceAsOf = System.currentTimeMillis();

		ArrayList<Customer> customers = new ArrayList<Customer>();
		// populate sample
		Customer c1 = new Customer();
		c1.accountId = "CAX5PNCPKC";
		c1.firstName = "Slawomir";
		c1.lastName = "Lisznianski";
		c1.title = "Creative Guru";
		c1.identifier = "123-123-3123";
		c1.mugshot = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.contrib_sl);
		c1.balance = new Amount(5632, -2);
		c1.balanceAsOf = balanceAsOf;
		customers.add(c1);

		Customer c2 = new Customer();
		c2.accountId = "CAX5RNCPKC";
		c2.firstName = "Eng";
		c2.lastName = "Choong";
		c2.title = "Coding Ninja";
		c2.identifier = "221-823-3123";
		c2.mugshot = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.contrib_ec);
		c2.balance = new Amount(15632, -2);
		c2.balanceAsOf = balanceAsOf;
		customers.add(c2);

		Customer c3 = new Customer();
		c3.accountId = "KAX5RNCPKC";
		c3.firstName = "Gilbert";
		c3.lastName = "Cheung";
		c3.title = "Hardware Hacker";
		c3.identifier = "331-823-3123";
		c3.mugshot = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.contrib_gc);
		c3.balance = new Amount(8810, -2);
		c3.balanceAsOf = balanceAsOf;
		customers.add(c3);

		Customer c4 = new Customer();
		c4.accountId = "LAX5RNCPKC";
		c4.firstName = "Lucas";
		c4.lastName = "Lisznianski";
		c4.title = "8th Grade Student";
		c4.identifier = "331-823-3123";
		c4.mugshot = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.contrib_lucas);
		c4.balance = new Amount(810, -2);
		c4.balanceAsOf = balanceAsOf;
		customers.add(c4);

		Customer c5 = new Customer();
		c5.accountId = "JJX5RNCPKC";
		c5.firstName = "Milosh";
		c5.lastName = "Lisznianski";
		c5.title = "4th Grade Student";
		c5.identifier = "881-823-3123";
		c5.mugshot = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.contrib_milosh);
		c5.balance = new Amount(100, -2);
		c5.balanceAsOf = balanceAsOf;
		customers.add(c5);

		res.setCustomers(customers);
		return res;
	}
}
