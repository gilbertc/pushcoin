package com.pushcoin.bitsypos;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import android.util.Log;

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
		Log.v(Conf.TAG, "place=cart;add_item="+item.getName() );
		items_.add(item);

		// broadcast cart content has changed
		Message m = dispatchable_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
	}

	void insert( Item item, int position )
	{
		Log.v(Conf.TAG, "place=cart;insert_item="+item.getName() );
		if ( position > items_.size() ) {
			items_.add(item);
		}
		else {
			items_.add(position, item);
		}

		// broadcast cart content has changed
		Message m = dispatchable_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
	}

	void remove( int position )
	{
		Item item = items_.remove(position);
		Log.v(Conf.TAG, "place=cart;remove_item="+item.getName()+";at-pos="+position);
		// broadcast cart content has changed
		Message m = dispatchable_.obtainMessage(MessageId.CART_CONTENT_CHANGED, 0, 0);
		m.sendToTarget();
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
