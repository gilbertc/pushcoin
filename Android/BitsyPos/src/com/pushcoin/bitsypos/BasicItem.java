package com.pushcoin.bitsypos;

import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
	Implements simple item, one which doesn't hold any children.

	For more information, see Item.java
*/
public class BasicItem implements Item
{
	public BasicItem ( String itemId, String name, BigDecimal price, Map<String, String> properties )
	{
		itemId_ = itemId;
		name_ = name;
		price_ = price;
		properties_ = unmodifiableMap( properties );
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String toString() {
		return name_;
	}

	@Override
	public String getId() {
		return itemId_;
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public BigDecimal getPrice() {
		return price_;
	}

	@Override
	public BigDecimal basePrice() {
		return price_;
	}

	@Override
	public List<Item> getAlternatives() {
		return ItemHelper.ZERO_ITEMS;  
	}

	@Override
	public List<Item> getRelatedItems() {
		return help_.getRelatedItems( Conf.FIELD_PRICE_TAG_DEFAULT, itemId_ );
	}

	@Override
	public List<Item> getChildren() {
		return ItemHelper.ZERO_ITEMS;  
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Item remove(int index) {
		throw new BitsyError("remove: basic-item cannot remove itself");
	}

	@Override
	public Item replace(int index, Item item) 
	{
		if (index	> 0) {
			throw new BitsyError("replace: no child-item to replace at position other than zero");
		}
		// we return this new item, in place of us
		return item;
	}

	@Override
	public Item append(Item item)
	{
		// transitioning to a combo
		return new ComboItem( "", "", Conf.ZERO_PRICE, help_.ZERO_PROPS, asList(this, item) );
	}

	/**
		Returns properties (key:value) associated with this item.
	*/
	@Override
	public Map<String, String> getProperties() {
		return properties_;
	}

	/**
		Returns a new item with this set of properties.
	*/
	@Override
	public Item setProperties( Map<String, String> properties ) {
		return new BasicItem( itemId_, name_, price_, properties);
	}

	private final String itemId_;
	private final String name_;
	private final BigDecimal price_;
	private final Map<String, String> properties_;

	private List<Item> relatedItems_ = null;
	private final ItemHelper help_ = new ItemHelper();
}
