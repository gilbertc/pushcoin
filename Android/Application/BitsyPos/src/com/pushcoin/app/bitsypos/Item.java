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

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import android.os.Parcelable;

/**
	Defines an Item interface.

	Items have these characteristics:

	- Item is immutable and can be one of: basic, combo or a slot.
	- If item has children, it is a combo-item.
	- A slot-item is a placeholder for an item that needs to be filled with an 
	  actual item, basic or combo.
	- If item is not a slot item nor a combo item, it's a basic item.
	- A 'defined item' refers to an item with a price (ie all slots are filled).
*/
public interface Item extends Parcelable
{
	/**
		Pretty-name of this item.
	*/
	String getName();

	/**
		Debugging assistance to show item name.
	*/
	String toString();

	/**
		Item unique ID (SKU, product ID, etc)
	*/
	String getId();

	/**
		Color to apply to a frame or background when 
		rendering this item in the catalog.
	*/
	String getTint();

	/**
		Return true if item can be priced.
	*/
	boolean isDefined();

	/**
		Returns a total price for this item - base plus all children.
	*/
	BigDecimal getPrice();

	/**
		Base price, without children if any.
	*/
	BigDecimal basePrice();

	/**
		Returns items declared suitable replacements for this item.
	*/
	List<Item> getAlternatives();

	/**
		Returns related items.
	*/
	List<Item> getRelatedItems();

	/**
		Returns children of this item, or empty list.
	*/
	List<Item> getChildren();

	/**
		Returns true if this item has children.
	*/
	boolean hasChildren();

	/**
		Returns a new item with a child replaced at an index.
	*/
	Item replace(int index, Item item);

	/**
		Returns a new item with a child deleted.
	*/
	Item remove(int index);

	/**
		Returns a new item with a child appended.
	*/
	Item append(Item item);

	/**
		Returns true if item holds any properties.
	*/
	boolean hasProperties();

	/**
		Returns properties (key:value) associated with this item.
	*/
	Map<String, String> getProperties();

	/**
		Returns a new item with provided properties.
	*/
	Item setProperties(	Map<String, String> properties );
}
