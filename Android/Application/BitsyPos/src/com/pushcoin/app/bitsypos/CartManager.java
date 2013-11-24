package com.pushcoin.app.bitsypos;

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
		final Date tmCreate;
		boolean active;

		private Entry(String name, Cart cart, boolean active)
		{
			this.name = name;
			this.cart = cart;
			this.tmCreate = new Date();
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
		return getActive().cart;
	}

	/**
		Returns an active cart or creates new one and nominates it as active.
	*/
	public Entry getActive()
	{
		Entry entry = findActive();	
		
		// If found, return right away
		if (entry != null) {
			return entry;
		}

		// If there is no active, find Main
		if (entry == null) {
			entry = findWithName(defaultCartName_);
		}

		// No Main? Pick oldest
		if (entry == null && !carts_.isEmpty()) {
			entry = carts_.get(0);
		}

		// Still nothing? Create a new one!
		if (entry == null) {
			entry = create( defaultCartName_, false );
		}
		
		// nominate as active, return
		return setActive( entry );	
	}

	/**
		Creates a new cart with a given name.
	*/
	public Entry create(String name, boolean active)
	{
		Log.v( Conf.TAG, "creating-new-cart|name="+name+";active="+active );
		// If the new entry is active, we de-activate an existing entry
		if ( active ) {
			clearActive();
		}
		Entry entry = Entry.newInstance( name, new Cart(), active ); 
		carts_.add( entry );
		EventHub.post( MessageId.CART_POOL_CHANGED );
		return entry;
	}

	/**
		Removes entry at position.
	*/
	public void remove(int position)
	{
		carts_.remove( position );
		EventHub.post( MessageId.CART_POOL_CHANGED );
	}

	/**
		Removes object equal to the one provided.
	*/
	public void remove(Entry e)
	{
		if (carts_.remove( e ) ) {
			EventHub.post( MessageId.CART_POOL_CHANGED );
		}
	}

	/**
		Returns entry at position.
	*/
	public Entry get( int position )
	{
		Entry entry = carts_.get( position );
		return entry;
	}

	/**
		Sets cart as 'active'.
	*/
	public Entry setActive( int position )
	{
		// clear previous active cart
		clearActive();
		// Find new entry and set as active
		Entry entry = carts_.get( position );
		entry.active = true;
		EventHub.post( MessageId.CART_POOL_CHANGED );
		Log.v( Conf.TAG, "switching-active-cart|name="+entry.name );
		return entry;
	}

	/**
		Sets cart as 'active'.
	*/
	private Entry setActive( Entry other )
	{
		// clear previous active cart
		clearActive();
		other.active = true;
		EventHub.post( MessageId.CART_POOL_CHANGED );
		Log.v( Conf.TAG, "switching-active-cart|name="+other.name );
		return other;
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
		Returns named cart
	*/
	public Entry findWithName(String name)
	{
		for (Entry e: carts_)
		{
			if ( e.name.equals(name) ) {
				return e; 
			}
		}
		return null;
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
