package com.pushcoin.bitsypos;

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

			if (entries.size() > 1) {
				return name;
			} else {
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
	
	public Cart( Context context ) 
	{
		parentContext_ = ((IDispatcher)context).getDispachable();
	}

	void add( Combo item )
	{
		Log.v(Conf.TAG, "cart-append-item="+item.getName() );
		synchronized (lock_) {
			items_.add(item);
		}

		// broadcast cart content has changed
		Message m = parentContext_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
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

		// broadcast cart content has changed
		Message m = parentContext_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
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
			// broadcast cart content has changed
			Message m = parentContext_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
			m.sendToTarget();
		}
		return item;
	}

	void clear()
	{
		boolean effective;
		synchronized (lock_) 
		{
			effective = !items_.isEmpty();
			items_.clear();
		}

		if (effective) 
		{
			// broadcast cart content has changed
			Message m = parentContext_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
			m.sendToTarget();
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

	private Handler parentContext_;
	private final Object lock_ = new Object();

	ArrayList<Combo> items_ = new ArrayList<Combo>();
}
