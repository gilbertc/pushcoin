package com.pushcoin.bitsypos;

import android.util.Log;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class Slot extends Item
{
	public Slot ( AppDb db, String parentItemId, String name, String defaultItemId, String choiceItemTag, int quantity, String priceTag ) 
	{
		super( db, parentItemId, name, null, null, 0 );
		parentItemId_ = parentItemId; 
		defaultItemId_ = defaultItemId;
		choiceItemTag_ = choiceItemTag;
		quantity_ = quantity;
		priceTag_ = priceTag;
	}

	/**
		Returns item-choices that can go into this slot.
	*/
	ArrayList<Item> getAlternatives() 
	{
		if ( alternativesCache_ == null && choiceItemTag_ != null ) {
			alternativesCache_ = db_.findItems( choiceItemTag_, priceTag_ );
		}
		return alternativesCache_;
	};

	/**
		Returns default item configured for this slot, NULL otherwise.
	*/
	Item getDefaultItem() 
	{
		if ( defaultItemCache_ == null && defaultItemId_ != null ) {
			defaultItemCache_ = db_.getItemById( defaultItemId_, priceTag_ );
		}
		return defaultItemCache_;
	}

	/**
		Returns a single defined-item.
	*/
	Item getChosenItem() {
		return chosenItem_;
	}

	/**
		Sets chosen item.
	*/
	void setChosenItem( Item item )
	{
		if ( item.isDefined( priceTag_ ) )
		{
			if ( Item.equivalent( item, getDefaultItem() ) || Item.exists( item, getAlternatives() ) ) {
				chosenItem_ = item;
			}
		}
		if ( chosenItem_ == null ) {
			throw new BitsyError( "Cannot accept undefined, non-default or non-alternative item" );
		}
	}

	String getPriceTag() {
		return priceTag_;
	}

	int getQuantity() {
		return quantity_;
	}

	private final String parentItemId_;
	private final String defaultItemId_;
	private final String choiceItemTag_;
	private final int quantity_;
	private final String priceTag_;

	private Item chosenItem_ = null;

	// -- cached DB objects
	private Item defaultItemCache_ = null;
	private ArrayList<Item> alternativesCache_ = null;
}
