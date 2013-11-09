package com.pushcoin.app.bitsypos;

import android.os.Parcel;
import android.os.Parcelable;
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
	public boolean hasProperties()
	{
		if (chosenItem_ == null)
			throw new BitsyError("No properties: undefined slot");

		return chosenItem_.hasProperties();
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

	/**
		Parcelable support below this point.

		Make sure to update code below when you add/remove 
		any class-member variables.
	*/
	private SlotItem( Parcel in )
	{
		parentItemId_ = in.readString();
		slotName_ = in.readString();
		slotPriceTag_ = in.readString();
		choiceItemTag_ = in.readString();
		chosenItem_ = in.readParcelable(null);
		alternatives_ = null;
	}

	@Override
	public void writeToParcel( Parcel out, int flags )
	{
		out.writeString( parentItemId_ );
		out.writeString( slotName_ );
		out.writeString( slotPriceTag_ );
		out.writeString( choiceItemTag_ );
		out.writeParcelable( chosenItem_, flags );
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<SlotItem> CREATOR = 
		new Parcelable.Creator<SlotItem>()
		{
			public SlotItem createFromParcel( Parcel in ) {
				return new SlotItem( in );
			}

			public SlotItem[] newArray( int size ) {
				return new SlotItem[size];
			}
		};
}
