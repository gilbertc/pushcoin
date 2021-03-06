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

import com.pushcoin.ifce.connect.data.Customer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public class Conf 
{
	static final String DATABASE_NAME = "bitsypos";
	static final int DATABASE_VERSION = 1;
	static final String FIELD_PRICE_TAG_DEFAULT = "unit";
	static final String PROPERTY_BOOL_TRUE = "Y";
	static final String PROPERTY_BOOL_FALSE = "N";
	static final String TAG = "Bitsy"; // Log tag
	static final String DIALOG_EDIT_CART="edit_cart_dialog";
	static final String DIALOG_EDIT_ITEM_PROPERTIES="edit_item_properties_dialog";
	static final String DIALOG_CONFIRM_CLEAR_CART="confirm_clear_cart_dialog";
	static final String DIALOG_PROMPT_TAB_NAME="prompt_tab_name_dialog";
	static final String CART_ITEM_EMPTY_NAME = "";
	static final String CART_ITEM_EMPTY_SKU = "";
	static final int CART_OPEN_ITEM_ID = -1;
	static final String PROPERTY_SEPARATOR = ":";
	static final String KEY_VALUE_SEPARATOR = "=";
	static final BigDecimal ZERO_PRICE = new BigDecimal("0.00");
	static final BigDecimal BIG_ZERO = new BigDecimal(0);
	static final BigDecimal BIG_ONE = new BigDecimal(1);
	static final BigDecimal BIG_HUNDRED = new BigDecimal(100);
	static final int MAX_COLS_IN_ITEM_PROPERTY_EDITOR = 5;
	static final List<Item> EMPTY_ITEM_LIST = unmodifiableList( new ArrayList<Item>() );
	static final List<Customer> EMPTY_CUSTOMER_LIST = unmodifiableList( new ArrayList<Customer>() );

	// Duration of time (ms) the user can undo removal of item from the cart. 
	static final int CART_UNDO_HIDE_DELAY=3000;

	/**
		Custom fonts in assets directory.
	*/
	static final String ASSET_FONT_ICOMOON = "fonts/icomoon.ttf";
	static final String ASSET_FONT_ENTYPO = "fonts/entypo.ttf";
	static final String ASSET_FONT_PUSHCOIN_COMPACT = "fonts/TT0467M.ttf";
	static final String ASSET_FONT_PUSHCOIN_REGULAR = "fonts/TT0468M.ttf";
	static final String ASSET_FONT_PUSHCOIN_BOLD = "fonts/TT0469M.ttf";

	/** 
		Database queries.  

		Excuse long-strings, but Java authors didn't care for multi-line literals:
		http://stackoverflow.com/questions/878573/java-multiline-string
	*/

	/**
		Fetch item by ID.
		
		Input:
			priceTag
			itemID
		
		select
			item.item_id,
			item.name,
			price.value,
			group_concat(props.name||"="||props.value,':') as prop,
			count(combo.slot_name) as slots,
			item.tint
		from item 
			join price on 
				item.item_id = price.item_id and price.price_tag = 'combo_priced_in'
			left join combo_item combo on 
				combo.parent_item_id = item.item_id
			left join item_property props on
				props.item_id = item.item_id
		where item.item_id = 'BSI1'
		group by item.item_id;
	*/
	static final String SQL_FETCH_ITEM_BY_ID = "select item.item_id, item.name, price.value, ifnull(group_concat(props.name||'='||props.value,':'), '') as prop, count(combo.slot_name) as slots, item.tint from item join price on item.item_id = price.item_id and price.price_tag = ? left join combo_item combo on combo.parent_item_id = item.item_id left join item_property props on props.item_id = item.item_id where item.item_id = ? group by item.item_id";

	/**
		Fetch item(s) by tag.
		
		Input:
			priceTag
			itemTag
		
		select 
			item.item_id, item.name, price.value, 
			ifnull(group_concat(props.name||"="||props.value,':'), "") as prop,
			count(combo.slot_name) as slots,
			item.tint
		from item 
			join tagged_item tagged on 
				item.item_id = tagged.item_id 
			join price on
				item.item_id = price.item_id 
				and price.price_tag = 'combo_priced_in'
			left join combo_item combo on
				combo.parent_item_id = item.item_id
			left join item_property props on
				props.item_id = item.item_id
		where tagged.tag_id = 'breakfast_special_item' 
		group by item.item_id
		order by item.sort_id ASC, item.name;
	*/
	static final String SQL_FETCH_ITEMS_BY_TAG = "select item.item_id, item.name, price.value, ifnull(group_concat(props.name||'='||props.value,':'), '') as prop, count(combo.slot_name) as slots, item.tint from item join tagged_item tagged on item.item_id = tagged.item_id join price on item.item_id = price.item_id and price.price_tag = ? left join combo_item combo on combo.parent_item_id = item.item_id left join item_property props on props.item_id = item.item_id where tagged.tag_id = ? group by item.item_id order by item.sort_id ASC, item.name";

	/**
		Fetch related item(s) of an item.

		Input:
			priceTag
			itemID
			itemID

		select 
			item.item_id, item.name, price.value, 
			ifnull(group_concat(props.name||"="||props.value,':'), "") as prop,
			count(combo.slot_name) as slots,
			item.tint
		from item
			join tagged_item tagged 
				on item.item_id = tagged.item_id 
			join price 
				on item.item_id = price.item_id
				and price.price_tag = 'unit'
			left join combo_item combo 
				on combo.parent_item_id = item.item_id
			left join item_property props on
				props.item_id = item.item_id
		where tagged.tag_id in
			(select tag_id from related_item where item_id = 'BSC1')
			and item.item_id != 'BSC1'
		group by item.item_id
		order by item.sort_id ASC, item.name;
	*/
	static final String SQL_FETCH_RELATED_ITEMS = "select item.item_id, item.name, price.value, ifnull(group_concat(props.name||'='||props.value,':'), '') as prop, count(combo.slot_name) as slots, item.tint from item join tagged_item tagged on item.item_id = tagged.item_id join price on item.item_id = price.item_id and price.price_tag = ? left join combo_item combo on combo.parent_item_id = item.item_id left join item_property props on props.item_id = item.item_id where tagged.tag_id in (select tag_id from related_item where item_id = ?) and item.item_id != ? group by item.item_id order by item.sort_id ASC, item.name";

	/**
		Fetch children in a combo item.

		Input:
			parentItemID

		This query doesn't return typical item-columns, as combos
		often hold "slots", which are empty, undefined item holders
		needing to be filled in by one of the choice-tagged items.

		select
			combo.parent_item_id, combo.slot_name, combo.price_tag, 
			ifnull(combo.choice_item_tag, ''),
			ifnull(combo.default_item_id, ''),
			default_item.name, default_item_price.value, 
			ifnull(group_concat(default_item_props.name||'='||default_item_props.value,':'), ''),
			count(default_item_combo.slot_name),
			default_item.tint
		from
			combo_item combo
			left join item default_item on
				combo.default_item_id = default_item.item_id
			left join price default_item_price on 
				combo.default_item_id = default_item_price.item_id 
				and combo.price_tag = default_item_price.price_tag
			left join item_property default_item_props on
				combo.default_item_id = default_item_props.item_id
			left join combo_item default_item_combo on 
				combo.default_item_id = default_item_combo.parent_item_id
		where 
			combo.parent_item_id = 'BSC1'
		group by combo.slot_name
		order by combo.slot_name;
	*/
	static final String SQL_GET_CHILDREN = "select combo.parent_item_id, combo.slot_name, combo.price_tag, ifnull(combo.choice_item_tag, ''), ifnull(combo.default_item_id, ''), default_item.name, default_item_price.value, ifnull(group_concat(default_item_props.name||'='||default_item_props.value,':'), ''), count(default_item_combo.slot_name), default_item.tint from combo_item combo left join item default_item on combo.default_item_id = default_item.item_id left join price default_item_price on combo.default_item_id = default_item_price.item_id and combo.price_tag = default_item_price.price_tag left join item_property default_item_props on combo.default_item_id = default_item_props.item_id left join combo_item default_item_combo on combo.default_item_id = default_item_combo.parent_item_id where combo.parent_item_id = ? group by combo.slot_name order by combo.slot_name";

	/**
		Returns item-categories that go into Bitsy's main menu.
	*/
	static final String SQL_GET_MAIN_CATEGORIES = "select category_id, tag_id from category";

	/**
		JSON import SQL
	*/
	static final String STMT_CATEGORY_INSERT = "insert into category (category_id, tag_id) values(?, ?)";
	static final String STMT_ITEM_INSERT = "insert into item (item_id, name, image, tint, sort_id) values(?, ?, ?, ?, ?)";
	static final String STMT_TAGGED_ITEM_INSERT = "insert into tagged_item ( tag_id, item_id ) values (?, ?)";
	static final String STMT_RELATED_ITEM_INSERT = "insert into related_item ( item_id, tag_id ) values (?, ?)";
	static final String STMT_ITEM_PROPERTY_INSERT = "insert into item_property ( item_id, name, value ) values (?, ?, ?)";
	static final String STMT_PRICE_INSERT = "insert into price (item_id, price_tag, value) values (?, ?, ?)";
	static final String STMT_COMBO_ITEM_INSERT= "insert into combo_item (parent_item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag) values (?, ?, ?, ?, ?, ?)";

	static final String[] PRODUCT_TABLES = new String[] { "category", "item", "tagged_item", "related_item", "price", "combo_item", "item_property" };
	static final String SAMPLE_DATA_FILE= "databases/example.json";

	/**
		JSON and Bundle fields
	*/
	static final String FIELD_CATEGORY = "category";
	static final String FIELD_NAME = "name";
	static final String FIELD_SLOT_NAME = "slot";
	static final String FIELD_SLOT_DEFAULT_ITEM = "default";
	static final String FIELD_SLOT_ITEM_TAG = "choice_tag";
	static final String FIELD_SLOT_PRICE_TAG = "price_tag";
	static final String FIELD_TAG = "tag";
	static final String FIELD_RELATED_ITEM = "related_item";
	static final String FIELD_PRODUCT = "product";
	static final String FIELD_ITEM_ID = "id";
	static final String FIELD_IMAGE = "image";
	static final String FIELD_TINT = "tint";
	static final String FIELD_ORDER = "order";
	static final String FIELD_PRICE = "price";
	static final String FIELD_COMBO = "combo";
	static final String FIELD_QUANTITY = "quantity";
	static final String FIELD_CART_ITEM_POSITION = "cart_item_position";
	static final String FIELD_ITEM_PROPERTY = "property";
	static final String FIELD_ITEM_CHILDREN = "children";
	static final String FIELD_ITEM = "item";

	/**
		Maps item-fields within a DB cursor.
	*/
	static final Map<String, Integer> ITEM_IN_CURSOR_T0;
	static 
	{
		Map<String, Integer> tmpITEM_IN_CURSOR_T0 = new HashMap<String, Integer>();

		tmpITEM_IN_CURSOR_T0.put(FIELD_ITEM_ID, 0);
		tmpITEM_IN_CURSOR_T0.put(FIELD_NAME, 1);
		tmpITEM_IN_CURSOR_T0.put(FIELD_PRICE, 2);
		tmpITEM_IN_CURSOR_T0.put(FIELD_ITEM_PROPERTY, 3);
		tmpITEM_IN_CURSOR_T0.put(FIELD_ITEM_CHILDREN, 4);
		tmpITEM_IN_CURSOR_T0.put(FIELD_TINT, 5);

		ITEM_IN_CURSOR_T0 = unmodifiableMap( tmpITEM_IN_CURSOR_T0 );
	}

	static final Map<String, Integer> ITEM_IN_CURSOR_T1;
	static 
	{
		Map<String, Integer> tmpITEM_IN_CURSOR_T1 = new HashMap<String, Integer>();

		tmpITEM_IN_CURSOR_T1.put(FIELD_ITEM_ID, 4);
		tmpITEM_IN_CURSOR_T1.put(FIELD_NAME, 5);
		tmpITEM_IN_CURSOR_T1.put(FIELD_PRICE, 6);
		tmpITEM_IN_CURSOR_T1.put(FIELD_ITEM_PROPERTY, 7);
		tmpITEM_IN_CURSOR_T1.put(FIELD_ITEM_CHILDREN, 8);
		tmpITEM_IN_CURSOR_T1.put(FIELD_TINT, 9);

		ITEM_IN_CURSOR_T1 = unmodifiableMap( tmpITEM_IN_CURSOR_T1 );
	}

	static final Map<String, Integer> COLOR_TINT_MAP;
	static 
	{
		Map<String, Integer> tmp = new HashMap<String, Integer>();

		tmp.put("red", R.color.tint_red);
		tmp.put("cyan", R.color.tint_cyan);
		tmp.put("blue", R.color.tint_blue);
		tmp.put("purple", R.color.tint_purple);
		tmp.put("magenta", R.color.tint_magenta);
		tmp.put("orange", R.color.tint_orange);
		tmp.put("green", R.color.tint_green);
		tmp.put("olive", R.color.tint_olive);

		COLOR_TINT_MAP = unmodifiableMap( tmp );
	}
}
