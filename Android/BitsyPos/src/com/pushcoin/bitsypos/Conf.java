package com.pushcoin.bitsypos;

public class Conf 
{
	static final String DATABASE_NAME = "bitsypos";
	static final int DATABASE_VERSION = 1;

	static final String TAG = "Bitsy"; // Log tag
	static final String SCREEN_LOCK_TAG = "Bitsy-Does-Not-Sleep";

	/** 
		Database queries.  
		I wish this section was more readable but Java people obviously don't
		appreciate multi-line literals.
		http://stackoverflow.com/questions/878573/java-multiline-string
	*/

	static final String SQL_FIND_ITEM_BY_ID = "select item.name from item where item_id = ?";
	static final String SQL_FIND_ITEM_BY_TAG = "select item.item_id, item.name from tagged_item tagged join item on item.item_id = tagged.item_id where tagged.tag_id = ?";
	static final String SQL_FIND_RELATED_ITEMS = "select distinct item.item_id, item.name from tagged_item tagged join item on tagged.item_id = item.item_id where tagged.tag_id in (select item_tag from related_item where item_id = ?) and item.item_id != ?";
	static final String SQL_GET_SLOTS = "select parent_item_id, combo_item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag from combo_item where parent_item_id = ?";

	static class BitsyError extends RuntimeException 
	{
		public BitsyError(final String message) {
      super(message);
		} 
	}
}
