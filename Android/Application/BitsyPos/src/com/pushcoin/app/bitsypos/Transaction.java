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

package com.pushcoin.app.bitsypos;

import com.pushcoin.ifce.connect.data.Customer;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.lang.System;

public class Transaction
{
	public final int STATUS_PENDING = 1;
	public final int STATUS_APPROVED = 2;
	public final int STATUS_DENIED = 4;

	public Transaction( BigDecimal amount )
	{
		clientTransactionId_ = UUID.randomUUID();
		amount_ = amount;
		exchangeTransactionId_ = "";
		createTime_ = System.currentTimeMillis();
		exchangeTime_ = 0;
		status_ = STATUS_PENDING;
		customer_ = null;
	}

	public Transaction approved( String exchangeTransactionId, long exchangeTime ) {
		return new Transaction(this, exchangeTransactionId, exchangeTime, STATUS_APPROVED);
	}

	public Transaction denied( String exchangeTransactionId, long exchangeTime ) {
		return new Transaction(this, exchangeTransactionId, exchangeTime, STATUS_DENIED);
	}

	public Transaction setCustomer(Customer customer) {
		return new Transaction(this, customer);
	}

	public Customer getCustomer() {
		return customer_;
	}

	public BigDecimal getAmount() {
		return amount_;
	}

	public String getExchangeTransactionId() {
		return exchangeTransactionId_;
	}

	public long getExchangeTransactionTime() {
		return exchangeTime_;
	}

	public boolean isApproved() {
		return status_ == STATUS_APPROVED;
	}

	/**
		Returns local transaction identifier.
	*/
	String getClientTransactionId() {
		return clientTransactionId_.toString();
	}

	private Transaction( Transaction parent )
	{
		this(parent, parent.exchangeTransactionId_, parent.exchangeTime_, parent.status_, parent.customer_);
	}

	private Transaction(Transaction parent, String exchangeTransactionId, long exchangeTime, int status)
	{
		this(parent, exchangeTransactionId, exchangeTime, status, parent.customer_);
	}

	private Transaction(Transaction parent, Customer customer )
	{
		this(parent, parent.exchangeTransactionId_, parent.exchangeTime_, parent.status_, customer);
	}

	private Transaction(Transaction parent, String exchangeTransactionId, long exchangeTime, int status, Customer customer )
	{
		clientTransactionId_ = parent.clientTransactionId_;
		amount_ = parent.amount_;
		createTime_ = parent.createTime_;

		exchangeTransactionId_ = exchangeTransactionId;
		exchangeTime_ = exchangeTime;
		status_ = status;
		customer_ = customer;
	}

	private final UUID clientTransactionId_;
	private final BigDecimal amount_;
	private final String exchangeTransactionId_;
	private final long createTime_;
	private final long exchangeTime_;
	private final int status_;
	private final Customer customer_;
}
