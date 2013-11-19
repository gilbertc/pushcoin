package com.pushcoin.app.bitsypos;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.text.NumberFormat;

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
		String getPrettyPrice() 
		{
			return NumberFormat.getCurrencyInstance().format( getPrice() );
		}

		// ctor
		Combo()
		{
			note = "";
			name = "";
			basePrice = new BigDecimal(0);
		}

		// copy ctor
		Combo( Combo rhs )
		{
			for (Entry e: rhs.entries) {
				entries.add( new Entry( e ) );
			}
			note = rhs.note;
			name = rhs.name;
			basePrice = rhs.basePrice;
		}
	}

	public static class Transaction
	{
		Customer customer;
		String transactionId;
		BigDecimal amount;
	}
	
	void add( Combo item )
	{
		Log.v(Conf.TAG, "cart-append-item="+item.getName() );
		synchronized (lock_) {
			items_.add(item);
		}

		// broadcast cart content has changed
		EventHub.post( MessageId.CART_CONTENT_CHANGED );
	}

	void insert( Combo item, int position )
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
		EventHub.post( MessageId.CART_CONTENT_CHANGED );
	}

	Combo remove( int position )
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
			EventHub.post( MessageId.CART_CONTENT_CHANGED );
		}
		return item;
	}

	void replace( Combo item, int position )
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
		EventHub.post( MessageId.CART_CONTENT_CHANGED );
	}

	void clear()
	{
		boolean effective;
		synchronized (lock_) 
		{
			effective = !items_.isEmpty();
			items_.clear();
		}

		if (effective) {
			EventHub.post( MessageId.CART_CONTENT_CHANGED );
		}
	}

	/**
		Retrieves an item at a position.
	*/
	Combo get( int position )
	{
		synchronized (lock_) {
			return items_.get( position );
		}
	}

	/**
		Returns number of items in cart.
	*/
	int size() 
	{
		synchronized (lock_) {
			return items_.size();
		}
	}

	BigDecimal totalValue()
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

	BigDecimal totalProcessed()
	{
		synchronized (lock_) 
		{
			BigDecimal total = new BigDecimal(0);
			for (Transaction tx: transactions_) {
				total = total.add( tx.amount );
			}
			return total;
		}
	}

	BigDecimal amountDue()
	{
		BigDecimal due = totalValue().subtract( totalProcessed() );
		return ( due.compareTo( Conf.ZERO_PRICE) < 0 ) ? Conf.ZERO_PRICE: due;
	}

	private Handler parentContext_;
	private final Object lock_ = new Object();

	ArrayList<Combo> items_ = new ArrayList<Combo>();
	ArrayList<Transaction> transactions_ = new ArrayList<Transaction>();
}
