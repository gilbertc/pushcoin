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

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.List;

/**
	Common code for item implementations.
*/
public class ItemHelper
{
	public List<Item> getRelatedItems( String priceTag, String itemId ) 
	{
		// don't go to DB if already tried
		if (relatedItems_ == null)
		{
			final AppDb db = AppDb.getInstance();
			Cursor c = db.getReadableDatabase().
				rawQuery( Conf.SQL_FETCH_RELATED_ITEMS, 
					new String[]{ priceTag, itemId, itemId } );

			relatedItems_ = db.createItemsFromCursor( c, new AppDb.ItemFromCursorAdapter()
				{
					@Override
					public Item make( Cursor c ) {
						return db.createItemFromCursor(c, Conf.ITEM_IN_CURSOR_T0);
					}
				});
		}

		return relatedItems_;
	}

	private List<Item> relatedItems_ = null;

	static final List<Item> ZERO_ITEMS = Collections.<Item>emptyList();
	static final Map<String,String> ZERO_PROPS = Collections.<String, String>emptyMap();
}
