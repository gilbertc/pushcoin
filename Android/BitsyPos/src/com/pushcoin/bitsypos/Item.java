package com.pushcoin.bitsypos;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/**
	Defines an Item, which represents a sellable good.

	Axioms of an Item...

	1. Every item which has at least one "slot" is a "combo-item". A slot 
		determines quantity and the price-tag of the item within the slot. 
		Arbitrary nesting of items is permitted and possible thanks to slots.

	2. A fully defined combo-item has:
		- all its slots featuring a single "defined" item

		A "defined-item" can be added to the cart; it must have all of:
			- an underlying product (ID), and
			- a price,

	3. The simplest form of an item does not have slots and must be "defined".

	---
	Typical usage:

	AppDb db = getInstance(..);
	Items list = db.findItems( "breakfast" );
	
	for item in list:
		GridCell gc:
			// items which are 'defined' go straight to the Cart if clicked.
			// other items require further "drilling"...
			item.isDefined('unit')
			if defined:
				item.getPrice()
		addToGrid( gc );

	// Say user clicks on an 'undefined-item', then we enumerate its slots:

	Slot[] slots = undefined_item.getSlots()
	for slot in slots:
		Header name = slot.getName()
			// items which are 'defined' go straight to the Cart if clicked.
			// other items require further "drilling"...
			slot.isDefined( slot.getPriceTag() )
			if defined:
				item.getPrice()
			Item[] items = slot.getAlternatives();
		addToGrid( gc );
	
*/
public class Item
{
	public Item ( AppDb db, String itemId, String name )
	{
		db_ = db;
		itemId_ = itemId;
		name_ = name;
	}

	public String getName() {
		return name_;
	}

	public String getId() {
		return itemId_;
	}

	/**
		Tells if this item is a "defined-item" (see description above).
	*/
	boolean isDefined( String priceTag ) {
		return false;
	}

	/**
		Returns slots configured for this item.
	*/
	ArrayList<Slot> getSlots() 
	{
		Cursor c = db_.getReadableDatabase().rawQuery( Conf.SQL_GET_SLOTS, new String[]{itemId_} );

		ArrayList<Slot> rs = new ArrayList<Slot>();

		if ( !c.moveToFirst() ) {
			return rs;
		}

		do 
		{
			rs.add( new Slot(db_, c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5)) );
			Log.v(Conf.TAG, "slot|name="+c.getString(2) );
		}
		while (c.moveToNext());

		return rs;
	}

	/**
		Returns related items.
	*/
	ArrayList<Item> getRelatedItems() 
	{
		Cursor c = db_.getReadableDatabase().rawQuery( Conf.SQL_FIND_RELATED_ITEMS, new String[]{itemId_, itemId_} );

		ArrayList<Item> rs = new ArrayList<Item>();

		if ( !c.moveToFirst() ) 
		{
			Log.v( Conf.TAG, "empty-related-item|item_id="+itemId_ );
			return rs;
		}

		do 
		{
			rs.add( new Item(db_, c.getString(0), c.getString(1)) );
			Log.v(Conf.TAG, "related-item|item_id="+c.getString(0) + ";name="+c.getString(1) );
		}
		while (c.moveToNext());

		return rs;
	}

	protected Item getItemById( String itemId )
	{
		Cursor c = db_.getReadableDatabase().rawQuery( Conf.SQL_FIND_ITEM_BY_ID, new String[]{itemId_} );

		if ( !c.moveToFirst() ) {
			throw new Conf.BitsyError( "Item not found with ID: " + itemId );
		}
		return new Item(db_, itemId, c.getString(0));
	}

	static boolean exists( Item item, Iterable<Item> container )
	{
		if ( item != null && container != null ) 
		{
			for ( Item member : container )
			{
				if ( item.getId() == member.getId() ) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean equivalent( Item lhs, Item rhs )
	{
		if ( lhs != null && rhs != null ) {
			return (lhs.getId() == rhs.getId());
		}
		return false;
	}

	protected final AppDb db_;
	private final String itemId_;
	private final String name_;
}
