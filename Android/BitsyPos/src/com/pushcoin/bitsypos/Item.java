package com.pushcoin.bitsypos;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.math.BigDecimal;

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

*/
public class Item
{
	public Item ( AppDb db, String itemId, String name, String priceTag, BigDecimal price, int slotCount )
	{
		db_ = db;
		itemId_ = itemId;
		name_ = name;
		cachedPriceTag_ = priceTag;
		cachedPrice_ = price;
		slotCount_ = slotCount;
	}

	public String getName() {
		return name_;
	}

	public String toString() {
		return name_;
	}

	public String getId() {
		return itemId_;
	}

	/**
		Tells if this item is a "defined-item" (see description above).
	*/
	boolean isDefined( String priceTag ) 
	{
		// Presence of a price means item is defined.
		try { 
			getPrice( priceTag );
		} 
		catch (BitsyError e) {
			return false;
		}
		return true;
	}

	/**
		Returns price formatted according to currency precision.
	*/
	String getPrettyPrice( String priceTag ) 
	{
		try {
			return NumberFormat.getCurrencyInstance().format( getPrice( priceTag ) );
		}
		catch (BitsyError e) {
			return "";
		}
	}

	/**
		Returns price for item based on a given tag.

		Pricing rules:
			- If (combo) item defines a unit price, we use it as a base.
			- Then, we add to the base by iterating over slots. 
				We either use the slot's "price_tag", or the supplied-tag.
	*/
	BigDecimal getPrice( String priceTag )
	{
		BigDecimal price = optionalPrice( priceTag );

		// Furthermore, if item has slots we need to iterate over
		if ( slotCount_ > 0 )
		{
			ArrayList<Slot> slots = getSlots();

			if ( price == null ) {
				price = new BigDecimal(0); 
			}

			for ( Slot slot : slots ) 
			{
				String slotPriceTag = slot.getPriceTag();
				if (slotPriceTag == null) {
					slotPriceTag = priceTag;
				}

				if (slot.getQuantity() < 1) {
					throw new BitsyError("Slot '" + slot.getName() + "' of item '" + getName() + "' is missing quantity");
				}

				price = price.add( ( slot.getPrice( slotPriceTag ).multiply( new BigDecimal( slot.getQuantity() ) ) ) );
			}
		}

		if ( price == null ) {
			throw new BitsyError( "Item " + getName() + " is missing a '"+ priceTag + "' price");
		}

		return price;
	}

	/**
		Returns slots configured for this item.
	*/
	ArrayList<Slot> getSlots() 
	{
		if (slotCount_ > 0 && slots_ == null)
		{
			slots_ = new ArrayList<Slot>();

			Cursor c = db_.getReadableDatabase().rawQuery( Conf.SQL_GET_SLOTS, new String[]{itemId_} );

			try 
			{
				if ( !c.moveToFirst() ) {
					return slots_;
				}

				do 
				{
					// default slot item can be null
					String defaultItemId = c.isNull(2) ? null : c.getString(2);
					String choiceItemTag = c.isNull(3) ? null : c.getString(3);
					if (defaultItemId == null && choiceItemTag == null) {
						throw new BitsyError("Slot '" + c.getString(0) + "' of item '" + getName() + "' is missing a default item and has no alternative choices");
					}

					int quantity = c.getInt(4);
					if (quantity < 1) {
						throw new BitsyError("Slot '" + c.getString(0) + "' of item '" + getName() + "' has zero item quantity");
					}

					// Slots may override 'unit' price
					String slotPriceTag = c.isNull(5) ? Conf.FIELD_PRICE_TAG_DEFAULT : c.getString(5);

					Slot slot = new Slot(db_, c.getString(0), c.getString(1), defaultItemId, choiceItemTag, quantity, slotPriceTag);
					slots_.add( slot );
					Log.v(Conf.TAG, "slot|parent="+getName()+";name="+slot.getName()+";default_item_id="+defaultItemId+";choice_item_tag="+choiceItemTag+";qty="+quantity+";price_tag="+slotPriceTag );
				}
				while (c.moveToNext());

			} finally {
				c.close();
			}
		}

		return slots_;
	}

	/**
		Returns true if item is a "combo item".
	*/

	boolean isCombo()
	{
		return (slotCount_ > 0);
	}

	/**
		Returns related items.
	*/
	ArrayList<Item> getRelatedItems( String priceTag ) 
	{
		if (relatedItemsCache_ == null)
		{
			relatedItemsCache_ = new ArrayList<Item>();

			Cursor c = db_.getReadableDatabase().rawQuery( Conf.SQL_FETCH_RELATED_ITEMS, new String[]{ priceTag, itemId_, itemId_ } );
			try 
			{
				if ( !c.moveToFirst() ) 
				{
					Log.v( Conf.TAG, "empty-related-item|item_id="+itemId_ );
					return relatedItemsCache_;
				}

				do 
				{
					// price can be null
					String itemPriceTag = c.isNull(2) ? null : c.getString(2);
					BigDecimal itemPrice = c.isNull(3) ? null : new BigDecimal( c.getString(3) );

					relatedItemsCache_.add( new Item(db_, c.getString(0), c.getString(1), itemPriceTag, itemPrice, c.getInt(4) ) );
					Log.v(Conf.TAG, "related-item|item_id="+c.getString(0) + ";name="+c.getString(1) );
				}
				while (c.moveToNext());

			} finally {
				c.close();
			}
		}

		return relatedItemsCache_;
	}

	protected BigDecimal optionalPrice( String priceTag )
	{
		if (priceTag == null) {
			priceTag = Conf.FIELD_PRICE_TAG_DEFAULT;
		}

		BigDecimal price = null;
		if (cachedPriceTag_ != null && priceTag == cachedPriceTag_) {
			price = cachedPrice_;
		}
		else
		{
			Cursor c = db_.getReadableDatabase().rawQuery( Conf.SQL_GET_ITEM_PRICE, new String[]{itemId_} );
			try 
			{
				if ( c.moveToFirst() ) {
					price = new BigDecimal( c.getString(0) );
				}
			} finally {
				c.close();
			}
		}
		return price;
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
	private final String cachedPriceTag_;
	private final BigDecimal cachedPrice_;
	private final int slotCount_;

	private ArrayList<Item> relatedItemsCache_ = null;
	private ArrayList<Slot> slots_ = null;
}
