package com.pushcoin.bitsypos;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.math.BigDecimal;

public class Cart implements IDispatcher
{
	public Cart( Context context ) 
	{
		ctx_ = context;
		parentContext_ = ((IDispatcher)ctx_).getDispachable();
		items_ = new ArrayList<Item>();
	}

	void add( Item item )
	{
		Log.v(Conf.TAG, "place=cart;add_item="+item.getName() );
		synchronized (lock_) {
			items_.add(item);
		}

		// broadcast cart content has changed
		Message m = parentContext_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
	}

	void insert( Item item, int position )
	{
		Log.v(Conf.TAG, "place=cart;insert_item="+item.getName() );
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

	Item remove( int position )
	{
		Item item = null;
		synchronized (lock_) 
		{
			if ( position < items_.size() ) {
				item = items_.remove(position);
			}
		}
		if (item != null) 
		{
			Log.v(Conf.TAG, "place=cart;remove_item="+item.getName()+";at-pos="+position);
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
	Item get( int position )
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
			for (Item item: items_) {
				total = total.add( item.getPrice(Conf.FIELD_PRICE_TAG_DEFAULT) );
			}
			return total;
		}
	}

	/**
		Offloads callers by enquing actions rather than executing in-line.
	*/
	@Override
	public Handler getDispachable() 
	{
		return handler_;
	}
	
	/** Dispatch events. */
	private Handler handler_ = new Handler() {
		@Override
		public void handleMessage( Message msg ) 
		{
			Log.v(Conf.TAG, "place=cart;event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
			switch( msg.what )
			{
				case MessageId.CART_ADD_ITEM:
					add( (Item)msg.obj );
				break;
			}
		}
	};

	private Context ctx_;
	private Handler parentContext_;
	private final Object lock_ = new Object();

	ArrayList<Item> items_;
}
