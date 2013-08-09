package com.pushcoin.bitsypos;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;

public class Cart
{
	public Cart( Context context ) 
	{
		ctx_ = context;
		dispatchable_ = ((IDispatcher)ctx_).getDispachable();
		items_ = new ArrayList<Item>();
	}

	void add( Item item )
	{
		items_.add(item);

		// broadcast cart content has changed
		Message m = dispatchable_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
	}

	void remove( int position )
	{
		// ignore if out of bounds
		if ( position < items_.size() )
		{
			items_.remove(position);
			// broadcast cart content has changed
			Message m = dispatchable_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
			m.sendToTarget();
		}
	}

	/**
		Retrieves an item at a position.
	*/
	Item get( int position )
	{
		return items_.get( position );
	}

	/**
		Returns number of items in cart.
	*/
	int size() 
	{
		return items_.size();
	}

	private Context ctx_;
	private Handler dispatchable_;

	ArrayList<Item> items_;
}
