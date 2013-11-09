package com.pushcoin.app.bitsypos;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;
import android.database.Cursor;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.math.BigDecimal;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
	Implements a composite item, one which can hold children.

	For more information, see Item.java
*/
public class ComboItem implements Item
{
	public ComboItem ( String comboId, String comboName, BigDecimal basePrice, Map<String, String> properties, int childCount )
	{
		comboId_ = comboId;
		comboName_ = comboName;
		basePrice_ = basePrice;
		properties_ = unmodifiableMap( properties );
		childCount_ = childCount;
	}

	public ComboItem ( String comboId, String comboName, BigDecimal basePrice, Map<String, String> properties, List<Item> children )
	{
		comboId_ = comboId;
		comboName_ = comboName;
		basePrice_ = basePrice;
		properties_ = unmodifiableMap( properties );
		childCount_ = children.size();
		children_ = unmodifiableList( children );
	}

	@Override
	public String getName() {
		return comboName_;
	}

	@Override
	public String toString() {
		return comboName_;
	}

	@Override
	public String getId() {
		return comboId_;
	}

	/**
		Return true if item can be priced.
	*/
	@Override
	public boolean isDefined() 
	{
		try { 
			getPrice();
		} 
		catch (BitsyError e) {
			return false;
		}
		return true;
	}

	@Override
	public BigDecimal getPrice()
	{
		// Combo price is, recursively: base + SUM(child.price)
		BigDecimal price = basePrice();

		for ( Item child: getChildren() ) {
			price = price.add( ( child.getPrice() ) );
		}

		return price;
	}

	@Override
	public BigDecimal basePrice() {
		return basePrice_;
	}

	@Override
	public List<Item> getAlternatives() {
		return ItemHelper.ZERO_ITEMS;  
	}

	@Override
	public List<Item> getRelatedItems() {
		return help_.getRelatedItems( Conf.FIELD_PRICE_TAG_DEFAULT, comboId_ );
	}

	@Override
	public List<Item> getChildren()
	{
		if (childCount_ > 0 && children_ == null)
		{
			final AppDb db = AppDb.getInstance();
			Cursor c = db.getReadableDatabase().rawQuery( Conf.SQL_GET_CHILDREN, new String[]{comboId_} );

			children_ = 
				db.createItemsFromCursor( c, new AppDb.ItemFromCursorAdapter() {
					@Override
					public Item make( Cursor lc ) {
						return db.createSlotFromCursor(lc);
					}
				});
		}

		return children_;
	}

	/**
		Returns true if item is a "combo item".
	*/
	@Override
	public boolean hasChildren() {
		return (childCount_ > 0);
	}

	@Override
	public Item remove(int index)
	{
		// local list is unmodifiable, copy then remove
		ArrayList<Item> newChildren = new ArrayList<Item>(children_);
		newChildren.remove(index);
		return new ComboItem(comboId_, comboName_, basePrice_, properties_, newChildren);
	}

	@Override
	public Item replace(int index, Item item) 
	{
		// local list is unmodifiable, copy then replace
		ArrayList<Item> newChildren = new ArrayList<Item>(children_);
		newChildren.set(index, item);
		return new ComboItem(comboId_, comboName_, basePrice_, properties_, newChildren);
	}

	@Override
	public Item append(Item item)
	{
		// local list is unmodifiable, copy then append
		ArrayList<Item> newChildren = new ArrayList<Item>(children_);
		newChildren.add(item);
		return new ComboItem(comboId_, comboName_, basePrice_, properties_, newChildren);
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
	public Item setProperties( Map<String, String> properties ) 
	{
		// If we haven't yet fetched children, we create a combo
		// using only the child-count
		if (children_ == null) {
			return new ComboItem( comboId_, comboName_, basePrice_, properties, childCount_);
		} 
		else { 
			return new ComboItem( comboId_, comboName_, basePrice_, properties, children_);
		}
	}

	private final String comboId_;
	private final String comboName_;
	private final BigDecimal basePrice_;
	private final Map<String, String> properties_;
	private final int childCount_;

	private List<Item> children_ = null;
	private final ItemHelper help_ = new ItemHelper();

	/**
		Parcelable support below this point.

		Make sure to update code below when you add/remove 
		any class-member variables.
	*/
	private ComboItem( Parcel in )
	{
		comboId_ = in.readString();
		comboName_ = in.readString();
		basePrice_ = new BigDecimal( in.readString() );
		properties_ = Util.readPropertiesFromParcel( in, new TreeMap<String,String>() );
		childCount_ = in.readInt();
		if (childCount_ > 0) {
			in.readList( new ArrayList<Item>(), null );
		}
	}

	@Override
	public void writeToParcel( Parcel out, int flags )
	{
		out.writeString( comboId_ );
		out.writeString( comboName_ );
		out.writeString( basePrice_.toString() );
		Util.writePropertiesToParcel( out, properties_ );
		out.writeInt( childCount_ );
		if (childCount_ > 0) {
			out.writeList( children_ );
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ComboItem> CREATOR = 
		new Parcelable.Creator<ComboItem>()
		{
			public ComboItem createFromParcel( Parcel in ) {
				return new ComboItem( in );
			}

			public ComboItem[] newArray( int size ) {
				return new ComboItem[size];
			}
		};
}
