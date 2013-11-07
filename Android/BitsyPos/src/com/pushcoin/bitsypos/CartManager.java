package com.pushcoin.bitsypos;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.util.Log;

public class CartManager
{
	public static class Entry
	{
		final String name;
		final Cart cart;
		final Date tm_create;
		boolean active;

		private Entry(String name, Cart cart, boolean active)
		{
			this.name = name;
			this.cart = cart;
			this.tm_create = new Date();
			this.active = active;
		}

		static Entry newInstance(String name, Cart cart, boolean active) {
			return new Entry(name, cart, active);	
		}
	};

	public static class CartList extends ArrayList<Entry>
	{ }

	public static CartManager newInstance(Context ctx) 
	{
		if (inst_ == null) {
			inst_ = new CartManager(ctx);
		}
		return inst_;
	}

	public static CartManager getInstance() 
	{
		if (inst_ == null) {
			throw new BitsyError("Did you forget to call CartManager.newInstance in Activity?");
		}
		return inst_;
	}

	/**
		Returns default cart of the session.
	*/
	public Cart getActiveCart()
	{
		return getActiveEntry().cart;
	}

	/**
		Returns an active cart or creates new one and nominates it as active.
	*/
	public Entry getActiveEntry()
	{
		Entry entry = findActive();	

		// If there is no active, create a new one
		if (entry == null) {
			entry = createEntry( defaultCartName_, true );
		}
		return entry;
	}

	/**
		Creates a new cart with a given name.
	*/
	public Entry createEntry(String name, boolean active)
	{
		Log.v( Conf.TAG, "creating-new-cart|name="+name+";active="+active );
		// If the new entry is active, we de-activate an existing entry
		if ( active ) {
			clearActive();
		}
		Entry entry = Entry.newInstance( name, new Cart(), active ); 
		carts_.add( entry );
		if ( active ) {
			EventHub.post( MessageId.CART_CHANGED );
		}
		return entry;
	}

	/**
		Removes entry at position.
	*/
	public void removeEntry(int position) {
		Entry entry = carts_.remove( position );
	}

	/**
		Returns entry at position.
	*/
	public Entry getEntry( int position )
	{
		Entry entry = carts_.get( position );
		return entry;
	}

	/**
		Sets cart as 'active'.
	*/
	public Entry setActiveEntry( int position )
	{
		// clear previous active cart
		clearActive();
		// Find new entry and set as active
		Entry entry = carts_.get( position );
		entry.active = true;
		EventHub.post( MessageId.CART_CHANGED );
		Log.v( Conf.TAG, "switching-active-cart|name="+entry.name );
		return entry;
	}

	/**
		Returns number of carts (entries).
	*/
	public int size() {
		return carts_.size();
	}

	/**
		Clears 'active' state from a session entry.
	*/
	private void clearActive()
	{
		Entry e = findActive();
		if ( e != null ) {
			e.active = false;
		}
	}

	/**
		Returns 'active' cart.
	*/
	private Entry findActive()
	{
		for (Entry e: carts_)
		{
			if ( e.active ) {
				return e; 
			}
		}
		return null;
	}

	/**
	 * Constructor is private to prevent direct instantiation.
	 */
	private CartManager(Context ctx)
	{
		defaultCartName_ = ctx.getResources().getString(R.string.default_tab_name);
		carts_ = new CartList();
	}

	private final String defaultCartName_;
	private final CartList carts_;
	private static CartManager inst_ = null;
}
