package com.pushcoin.bitsypos;

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

	static final String TAG = "Bitsy"; // Log tag
	static final String DIALOG_EDIT_CART_ID="edit_cart_dialog";
	static final String CART_ITEM_EMPTY_NAME = "";
	static final String CART_ITEM_EMPTY_SKU = "";
	static final int CART_OPEN_ITEM_ID = -1;
	static final String PROPERTY_SEPARATOR = "\t";
	static final String KEY_VALUE_SEPARATOR = ":";
	static final BigDecimal ZERO_PRICE = new BigDecimal("0.00");

	// Duration of time (ms) the user can undo removal of item from the cart. 
	static final int CART_UNDO_HIDE_DELAY=3000;

	/**
		Session keys.
	*/
	static final String SESSION_CART = "cart";

	/** 
		Database queries.  
		I wish this section was more readable but Java people obviously don't
		appreciate multi-line literals.
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
			count(combo.slot_name) as slots
		from item 
			left join price 
				on item.item_id = price.item_id
				and price.price_tag = 'unit'
			left join combo_item combo 
				on combo.parent_item_id = item.item_id
		where item.item_id = 'GLS6'
		group by item.item_id
	*/
	static final String SQL_FETCH_ITEM_BY_ID = "select item.item_id, item.name, price.value, count(combo.slot_name) as slots from item left join price on item.item_id = price.item_id and price.price_tag = ? left join combo_item combo on combo.parent_item_id = item.item_id where item.item_id = ? group by item.item_id";

	/**
		Fetch item(s) by tag.
		
		Input:
			priceTag
			itemTag
		
		select 
			item.item_id,
			item.name,
			price.price_tag,
			price.value,
			count(combo.slot_name) as slots
		from item
			join tagged_item tagged 
				on item.item_id = tagged.item_id 
			left join price 
				on item.item_id = price.item_id
				and price.price_tag = 'unit'
			left join combo_item combo 
				on combo.parent_item_id = item.item_id
		where tagged.tag_id = 'breakfast'
		group by item.item_id
		order by item.name
	*/
	static final String SQL_FETCH_ITEMS_BY_TAG = "select item.item_id, item.name, price.price_tag, price.value, count(combo.slot_name) as slots from item join tagged_item tagged on item.item_id = tagged.item_id left join price on item.item_id = price.item_id and price.price_tag = ? left join combo_item combo on combo.parent_item_id = item.item_id where tagged.tag_id = ? group by item.item_id order by item.name";

	/**
		Fetch related item(s) of an item.

		Input:
			priceTag
			itemID (twice)

		select
			item.item_id,
			item.name,
			price.price_tag,
			price.value,
			count(combo.slot_name) as slots
		from item
			join tagged_item tagged 
				on item.item_id = tagged.item_id 
			left join price 
				on item.item_id = price.item_id
				and price.price_tag = 'unit'
			left join combo_item combo 
				on combo.parent_item_id = item.item_id
		where tagged.tag_id in
			(select tag_id from related_item where item_id = 'BSC1')
			and item.item_id != 'BSC1'
		group by item.item_id
		order by item.name
	*/
	static final String SQL_FETCH_RELATED_ITEMS = "select item.item_id, item.name, price.value, count(combo.slot_name) as slots from item join tagged_item tagged on item.item_id = tagged.item_id left join price on item.item_id = price.item_id and price.price_tag = ?  left join combo_item combo on combo.parent_item_id = item.item_id where tagged.tag_id in (select tag_id from related_item where item_id = ?) and item.item_id != ?  group by item.item_id order by item.name";

	static final String SQL_GET_CHILDREN = "select parent_item_id, slot_name, choice_item_tag, default_item_id from combo_item where parent_item_id = ? order by slot_name";
	static final String SQL_GET_MAIN_CATEGORIES = "select category_id, tag_id from category";
	static final String SQL_GET_ITEM_PRICE = "select value from price where item_id = ?";

	/**
		JSON import SQL
	*/
	static final String STMT_CATEGORY_INSERT = "insert into category (category_id, tag_id) values(?, ?)";
	static final String STMT_ITEM_INSERT = "insert into item (item_id, name, image) values(?, ?, ?)";
	static final String STMT_TAGGED_ITEM_INSERT = "insert into tagged_item ( tag_id, item_id ) values (?, ?)";
	static final String STMT_RELATED_ITEM_INSERT = "insert into related_item ( item_id, tag_id ) values (?, ?)";
	static final String STMT_ITEM_PROPERTY_INSERT = "insert into item_property ( item_id, name, value ) values (?, ?, ?)";
	static final String STMT_PRICE_INSERT = "insert into price (item_id, price_tag, value) values (?, ?, ?)";
	static final String STMT_COMBO_ITEM_INSERT= "insert into combo_item (parent_item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag) values (?, ?, ?, ?, ?, ?)";

	static final String[] PRODUCT_TABLES = new String[] { "category", "item", "tagged_item", "related_item", "price", "combo_item" };
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
	static final String FIELD_PRICE = "price";
	static final String FIELD_COMBO = "combo";
	static final String FIELD_QUANTITY = "quantity";
	static final String FIELD_CART_ITEM_POSITION = "cart_item_position";
	static final String FIELD_ITEM_PROPERTY = "property";
	static final String FIELD_ITEM_CHILDREN = "children";

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

		ITEM_IN_CURSOR_T0 = unmodifiableMap( tmpITEM_IN_CURSOR_T0 );
	}

	static final Map<String, Integer> ITEM_IN_CURSOR_T1;
	static 
	{
		Map<String, Integer> tmpITEM_IN_CURSOR_T1 = new HashMap<String, Integer>();

		tmpITEM_IN_CURSOR_T1.put(FIELD_ITEM_ID, 5);
		tmpITEM_IN_CURSOR_T1.put(FIELD_NAME, 6);
		tmpITEM_IN_CURSOR_T1.put(FIELD_PRICE, 7);
		tmpITEM_IN_CURSOR_T1.put(FIELD_ITEM_PROPERTY, 8);
		tmpITEM_IN_CURSOR_T1.put(FIELD_ITEM_CHILDREN, 9);

		ITEM_IN_CURSOR_T1 = unmodifiableMap( tmpITEM_IN_CURSOR_T1 );
	}
}
