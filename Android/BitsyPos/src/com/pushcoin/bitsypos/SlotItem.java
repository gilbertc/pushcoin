package com.pushcoin.bitsypos;

import android.util.Log;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.math.BigDecimal;
import static java.util.Collections.unmodifiableList;

public class SlotItem implements Item
{
	/**
		The goal of this constructor is to make a lightweight "proxy" 
		for the items it can store.

		Specifically, this constructor doesn't go to the database to
		fetch any underlying item-info. It will make DB calls only when
		any of its accessors is called; it will cache results too.
		This way, it's relatively cheap to construct plenty of combo-items
		regardless of their nesting-levels, slot counts or number of 
		alternatives per slot.
	*/
	public SlotItem( String parentItemId, String slotName, String slotPriceTag, String choiceItemTag, Item chosenItem ) 
	{
		parentItemId_ = parentItemId; 
		slotName_ = slotName; 
		slotPriceTag_ = slotPriceTag;
		choiceItemTag_ = choiceItemTag;
		chosenItem_ = chosenItem;
		alternatives_ = null;
	}

	public SlotItem( String parentItemId, String slotName, String slotPriceTag, String choiceItemTag, Item chosenItem, List<Item> alternatives ) 
	{
		parentItemId_ = parentItemId; 
		slotName_ = slotName; 
		slotPriceTag_ = slotPriceTag;
		choiceItemTag_ = choiceItemTag;
		chosenItem_ = chosenItem;
		alternatives_ = alternatives;
	}

	@Override
	public String getName() {
		return (chosenItem_ == null) ? slotName_ : chosenItem_.getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getId() {
		return (chosenItem_ == null) ? "" : chosenItem_.getId();
	}

	@Override
	public boolean isDefined()
	{
		try 
		{ 
			if (chosenItem_ != null && chosenItem_.getPrice() != null) {
				return true;
			}
		} catch (BitsyError e) { }
		return false;
	}

	@Override
	public BigDecimal getPrice()
	{
		if (chosenItem_ == null) 
			throw new BitsyError("No price: undefined slot");

		return chosenItem_.getPrice();
	}

	@Override
	public BigDecimal basePrice()
	{
		if (chosenItem_ == null)
			throw new BitsyError("No base-price: undefined slot");

		return chosenItem_.basePrice();
	}

	@Override
	public List<Item> getAlternatives()
	{
		// Go to DB only first time around
		if ( alternatives_ == null )
		{
			// First, fetch all choices
			AppDb db = AppDb.getInstance();
			List<Item> choiceItems = unmodifiableList( db.findItemsWithTag(choiceItemTag_, slotPriceTag_) );
			
			// Next, wrap each choice-item into a slot
			alternatives_ = new ArrayList<Item>();
			for ( Item choice: choiceItems ) {
				alternatives_.add( new SlotItem(parentItemId_, slotName_, slotPriceTag_, choiceItemTag_, choice, choiceItems) );
			}
		}
		return alternatives_;
	}

	@Override
	public List<Item> getRelatedItems()
	{
		if (chosenItem_ == null)
			throw new BitsyError("No related items: undefined slot");

		return chosenItem_.getRelatedItems();
	}

	@Override
	public List<Item> getChildren()
	{
		if (chosenItem_ == null)
			throw new BitsyError("No children: undefined slot");

		return chosenItem_.getChildren();
	}

	@Override
	public boolean hasChildren()
	{
		if (chosenItem_ == null)
			throw new BitsyError("No children: undefined slot");

		return chosenItem_.hasChildren();
	}

	@Override
	public Item remove(int index)
	{
		if (chosenItem_ == null)
			throw new BitsyError("Cannot remove: undefined slot");

		return new SlotItem( parentItemId_, slotName_, slotPriceTag_, choiceItemTag_, chosenItem_.remove(index), alternatives_);
	}

	@Override
	public Item replace(int index, Item item)
	{
		if (chosenItem_ == null)
			throw new BitsyError("Cannot replace: undefined slot");

		return new SlotItem( parentItemId_, slotName_, slotPriceTag_, choiceItemTag_, chosenItem_.replace(index, item), alternatives_);
	}

	@Override
	public Item append(Item item)
	{
		if (chosenItem_ == null)
			throw new BitsyError("Cannot append: undefined slot");

		return new SlotItem( parentItemId_, slotName_, slotPriceTag_, choiceItemTag_, chosenItem_.append(item), alternatives_);
	}

	@Override
	public Map<String, String> getProperties()
	{
		if (chosenItem_ == null)
			throw new BitsyError("No properties: undefined slot");

		return chosenItem_.getProperties();
	}

	@Override
	public Item setProperties( Map<String, String> properties ) 
	{
		if (chosenItem_ == null)
			throw new BitsyError("No properties: undefined slot");

		return new SlotItem( parentItemId_, slotName_, slotPriceTag_, choiceItemTag_, chosenItem_.setProperties( properties ), alternatives_);
	}

	private final String parentItemId_;
	private final String slotName_;
	private final String slotPriceTag_;
	private final String choiceItemTag_;
	private final Item chosenItem_;
	// Holds item-alternatives fetched from DB, but only after 
	// somebody asks for.
	private List<Item> alternatives_;
}
