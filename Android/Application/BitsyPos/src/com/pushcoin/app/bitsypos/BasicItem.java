/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.app.bitsypos;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.TreeMap;
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
	public BasicItem ( String itemId, String name, BigDecimal price, Map<String, String> properties, String tint )
	{
		itemId_ = itemId;
		name_ = name;
		tint_ = tint;
		price_ = price;
		properties_ = unmodifiableMap( properties );
	}

	@Override
	public String getName()
	{
		if ( !hasProperties() ) {
			return name_;
		} 
		else // show name with properties
		{
			StringBuilder name = new StringBuilder(name_);
			for ( Map.Entry<String, String> entry: getProperties().entrySet() )
			{
				if ( entry.getValue().equals( Conf.PROPERTY_BOOL_TRUE ) )
				{
					name.append( " " );
					name.append( entry.getKey() );
				}
			}
			return name.toString();
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getId() {
		return itemId_;
	}

	@Override
	public String getTint() {
		return tint_;
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
		return new ComboItem( "", "", Conf.ZERO_PRICE, help_.ZERO_PROPS, asList(this, item), "" );
	}

	@Override
	public boolean hasProperties() {
		return !properties_.isEmpty();
	}

	@Override
	public Map<String, String> getProperties() {
		return properties_;
	}

	@Override
	public Item setProperties( Map<String, String> properties ) {
		return new BasicItem( itemId_, name_, price_, properties, tint_ );
	}

	private final String itemId_;
	private final String name_;
	private final String tint_;
	private final BigDecimal price_;
	private final Map<String, String> properties_;

	private final ItemHelper help_ = new ItemHelper();

	/**
		Parcelable support below this point.

		Make sure to update code below when you add/remove 
		any class-member variables.
	*/
	private BasicItem( Parcel in )
	{
		itemId_ = in.readString();
		name_ = in.readString();
		tint_ = in.readString();
		price_ = new BigDecimal( in.readString() );
		properties_ = Util.readPropertiesFromParcel( in, new TreeMap<String,String>() );
	}

	@Override
	public void writeToParcel( Parcel out, int flags )
	{
		out.writeString( itemId_ );
		out.writeString( name_ );
		out.writeString( tint_ );
		out.writeString( price_.toString() );
		Util.writePropertiesToParcel( out, properties_ );
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<BasicItem> CREATOR = 
		new Parcelable.Creator<BasicItem>()
		{
			public BasicItem createFromParcel( Parcel in ) {
				return new BasicItem( in );
			}

			public BasicItem[] newArray( int size ) {
				return new BasicItem[size];
			}
		};
}
