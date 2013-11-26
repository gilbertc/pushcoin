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

public class Cart
{
	public static class Entry 
	{
		String sku;
		String name;
		int qty;
		BigDecimal unitPrice;

		// ctor
		Entry(String sku, String name, int qty, BigDecimal unitPrice)
		{
			this.sku = sku;
			this.name = name;
			this.qty = qty;
			this.unitPrice = unitPrice;
		}

		// copy ctor
		Entry( Entry rhs )
		{
			sku = rhs.sku;
			name = rhs.name;
			qty = rhs.qty;
			unitPrice = rhs.unitPrice;
		}
	}

	public static class Combo
	{
		ArrayList<Entry> entries = new ArrayList<Cart.Entry>();
		String note;
		String name;
		BigDecimal basePrice;

		String getName()
		{
			assert ( !entries.isEmpty() );

			if ( entries.size() > 1 ) {
				return name;
			} 
			else if ( entries.isEmpty() ) {
				return Conf.CART_ITEM_EMPTY_NAME;
			}
			else {
				return entries.get(0).name;
			}
		}

		BigDecimal getPrice() 
		{
			// start with base and keep adding slot prices
			BigDecimal total = basePrice;
			for (Entry entry: entries) {
				total = total.add( entry.unitPrice.multiply( new BigDecimal( entry.qty ) ) );
			}
			return total;
		}

		/**
			Returns price formatted according to currency precision.
		*/
		public String getPrettyPrice() 
		{
			return NumberFormat.getCurrencyInstance().format( getPrice() );
		}

		// ctor
		public Combo()
		{
			note = "";
			name = "";
			basePrice = new BigDecimal(0);
		}

		// copy ctor
		public Combo( Combo rhs )
		{
			for (Entry e: rhs.entries) {
				entries.add( new Entry( e ) );
			}
			note = rhs.note;
			name = rhs.name;
			basePrice = rhs.basePrice;
		}
	}

	public void add( Combo item )
	{
		Log.v(Conf.TAG, "cart-append-item="+item.getName() );
		synchronized (lock_) {
			items_.add(item);
		}
		resetChargeAmount(null);
		emitCartContentChanged();
	}

	public void insert( Combo item, int position )
	{
		Log.v(Conf.TAG, "cart-insert-item="+item.getName() );
		synchronized (lock_) 
		{
			if ( position > items_.size() ) {
				items_.add(item);
			}
			else {
				items_.add(position, item);
			}
		}
		resetChargeAmount(null);
		emitCartContentChanged();
	}

	public Combo remove( int position )
	{
		Combo item = null;
		synchronized (lock_) 
		{
			if ( position < items_.size() ) {
				item = items_.remove(position);
			}
		}
		if (item != null) 
		{
			Log.v(Conf.TAG, "cart-remove-item="+item.getName()+";at-pos="+position);
			resetChargeAmount(null);
			emitCartContentChanged();
		}
		return item;
	}

	public void replace( Combo item, int position )
	{
		Log.v(Conf.TAG, "cart-replace-item="+item.getName()+";pos="+position);
		synchronized (lock_) 
		{
			if (position < 0) {
				position = items_.size();
			} else {
				items_.remove( position );
			}

			items_.add(position, item);
		}
		resetChargeAmount(null);
		emitCartContentChanged();
	}

	public boolean isEmpty() {
		return items_.isEmpty();
	}

	public void clear()
	{
		boolean effective;
		synchronized (lock_) 
		{
			effective = !items_.isEmpty();
			items_.clear();
		}

		if (effective)
		{
			resetChargeAmount(null);
			emitCartContentChanged();
		}
	}

	/**
		Retrieves an item at a position.
	*/
	public Combo get( int position )
	{
		synchronized (lock_) {
			return items_.get( position );
		}
	}

	/**
		Returns number of items in cart.
	*/
	public int size() 
	{
		synchronized (lock_) {
			return items_.size();
		}
	}

	public BigDecimal totalValue()
	{
		synchronized (lock_) 
		{
			BigDecimal total = new BigDecimal(0);
			for (Combo item: items_) {
				total = total.add( item.getPrice() );
			}
			return total;
		}
	}

	/**
		Sums up amounts of approved transactions.
	*/
	public BigDecimal totalProcessed()
	{
		synchronized (lock_) 
		{
			BigDecimal total = new BigDecimal(0);
			for (Transaction tx: transactions_)
			{
				if ( tx.isApproved() ) {
					total = total.add( tx.getAmount() );
				}
			}
			return total;
		}
	}

	public BigDecimal amountDue()
	{
		BigDecimal due = totalValue().subtract(discount_).subtract( totalProcessed() );
		return ( due.compareTo( Conf.ZERO_PRICE) > 0 ) ? due : Conf.ZERO_PRICE;
	}

	public boolean isPaid() {
		return !(amountDue().compareTo( Conf.ZERO_PRICE ) > 0);
	}

	public void setChargeAmount(BigDecimal charge)
	{
		// fresh charge amount
		resetChargeAmount(charge);

		// time to broadcast
		emitCartContentChanged();
	}

	public BigDecimal getChargeAmount()
	{
		// Charge amount comes from either total due or a value set by user
		BigDecimal amountDue = amountDue();
		if ( chargeAmount_ == null || chargeAmount_.compareTo(amountDue) > 0) {
			chargeAmount_ = amountDue;
		}
		return chargeAmount_;
	}

	public void setDiscount(BigDecimal discount)
	{
		discount_ = discount;
		resetChargeAmount(null);
		emitCartContentChanged();
	}

	public BigDecimal getDiscount() {
		return discount_;
	}

	public BigDecimal getDiscountPct()
	{
		BigDecimal totalValue = totalValue();
		return (totalValue.compareTo( Conf.BIG_ZERO ) > 0)
			? discount_.divide( totalValue, 2, RoundingMode.HALF_UP ) : Conf.BIG_ZERO;
	}

	public void setDiscountPct(BigDecimal discount) throws NumberFormatException
	{
		if ( discount.compareTo(Conf.BIG_ZERO) < 0 || discount.compareTo(Conf.BIG_ONE) > 0) {
			throw new NumberFormatException("Discount percent must be in the range 0..1");
		}
		setDiscount( totalValue().multiply(discount) );
	}

	public Transaction createChargeTransaction()
	{
		BigDecimal chargeAmount = getChargeAmount();
		Transaction trx = null;
		if (chargeAmount.compareTo(Conf.BIG_ZERO) > 0)
		{
			trx = new Transaction( chargeAmount );
			transactions_.add( trx );
			emitTransactionChanged();
		}
		return trx;
	}

	public Transaction getTransaction( int position ) {
		return transactions_.get( position );
	}

	public Transaction findTransactionWithClientTransactionId( String key )
	{
		int pos = getPositionWithClientTransactionId( key );
		return (pos < 0) ? null : getTransaction( pos );
	}

	public void cancelTransaction( String clientTransactionId )
	{
		int pos = getPositionWithClientTransactionId( clientTransactionId );
		if (! (pos < 0))
		{
			Transaction transaction = getTransaction( pos );
			// cannot remove already approved transaction
			if (! transaction.isApproved() )
			{
				transactions_.remove( pos );
				emitTransactionChanged();
			}
		}
	}

	public void updateTransaction( Transaction newTransaction )
	{
		int pos = getPositionWithClientTransactionId( newTransaction.getClientTransactionId() );
		if (pos < 0) {
			transactions_.add(newTransaction);
		} else {
			transactions_.set(pos, newTransaction);
		}
		emitTransactionChanged();
	}

	public int totalTransactions() {
		return transactions_.size();
	}

	private int getPositionWithClientTransactionId( String otherId )
	{
		for ( int pos = 0; pos < transactions_.size(); ++pos ) {
			if (transactions_.get(pos).getClientTransactionId().equals(otherId)) {
				return pos;
			}
		}
		return -1;
	}

	private void resetChargeAmount( BigDecimal newCharge )
	{
		if (newCharge != null) 
		{
			if ( newCharge.compareTo(amountDue()) > 0 || !(newCharge.compareTo(Conf.BIG_ZERO) > 0)) {
				throw new BitsyError("Invalid charge amount");
			}
		}
		chargeAmount_ = newCharge;
	}

	private void emitCartContentChanged()
	{
		// broadcast cart content has changed
		EventHub.post( MessageId.CART_CONTENT_CHANGED );
	}

	private void emitTransactionChanged()
	{
		// broadcast cart content has changed
		EventHub.post( MessageId.TRANSATION_STATUS_CHANGED );
	}

	private Handler parentContext_;
	private final Object lock_ = new Object();

	ArrayList<Combo> items_ = new ArrayList<Combo>();
	ArrayList<Transaction> transactions_ = new ArrayList<Transaction>();
	BigDecimal discount_ = Conf.ZERO_PRICE;
	BigDecimal chargeAmount_ = null;
}
