package com.pushcoin.bitsypos;

public class Conf 
{
	static final String DATABASE_NAME = "bitsypos";
	static final int DATABASE_VERSION = 1;

	static final String TAG = "Bitsy"; // Log tag

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

	static final String SQL_FIND_ITEM_BY_ID = "select item.name from item where item_id = ? order by item.name";
	static final String SQL_FIND_ITEM_BY_TAG = "select item.item_id, item.name from tagged_item tagged join item on item.item_id = tagged.item_id where tagged.tag_id = ? order by item.name";
	static final String SQL_FIND_RELATED_ITEMS = "select distinct item.item_id, item.name from tagged_item tagged join item on tagged.item_id = item.item_id where tagged.tag_id in (select tag_id from related_item where item_id = ?) and item.item_id != ? order by item.name";
	static final String SQL_GET_SLOTS = "select parent_item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag from combo_item where parent_item_id = ? order by slot_name";
	static final String SQL_GET_MAIN_CATEGORIES = "select category_id, tag_id from category";

	/**
		JSON import SQL
	*/
	static final String STMT_CATEGORY_INSERT = "insert into category (category_id, tag_id) values(?, ?)";
	static final String STMT_ITEM_INSERT = "insert into item (item_id, name, image) values(?, ?, ?)";
	static final String STMT_TAGGED_ITEM_INSERT = "insert into tagged_item ( tag_id, item_id ) values (?, ?)";
	static final String STMT_RELATED_ITEM_INSERT = "insert into related_item ( item_id, tag_id ) values (?, ?)";
	static final String STMT_PRICE_INSERT = "insert into price (item_id, price_tag, value) values (?, ?, ?)";
	static final String STMT_COMBO_ITEM_INSERT= "insert into combo_item (parent_item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag) values (?, ?, ?, ?, ?, ?)";

	static final String[] PRODUCT_TABLES = new String[] { "category", "item", "tagged_item", "related_item", "price", "combo_item" };
	static final String SAMPLE_DATA_FILE= "databases/example.json";

	/**
		JSON fields
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
	static final String FIELD_PRICE_TAG_DEFAULT = "unit";

	static class BitsyError extends RuntimeException 
	{
		public BitsyError(final String message) {
      super(message);
		} 
	}
}
