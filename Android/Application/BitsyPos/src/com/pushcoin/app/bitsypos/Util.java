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

import android.database.Cursor;
import android.os.Parcel;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Typeface;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;
import java.util.Locale;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

class Util
{
	static public Cart.Combo toCartCombo(Item item)
	{
		Cart.Combo combo = new Cart.Combo();
		combo.basePrice = new BigDecimal(0); 

		if ( item.hasChildren() ) 
		{
			// Combo name and price
			combo.name = item.getName();
			combo.basePrice =  item.basePrice();

			for (Item child: item.getChildren()) 
			{
				Cart.Entry entry = new Cart.Entry(
					child.getId(), child.getName(), 1,
					child.getPrice());

				combo.entries.add( entry );
			}
		}
		else  // not a combo
		{
			Cart.Entry entry = new Cart.Entry(
				item.getId(), item.getName(), 1, item.getPrice());

			combo.entries.add( entry );
		}
		return combo;
	}

	static boolean exists( Item item, Iterable<Item> container )
	{
		if ( item != null && container != null ) 
		{
			for ( Item member : container )
			{
				if ( item.getId().equals( member.getId()) ) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean equivalent( Item lhs, Item rhs )
	{
		if ( lhs != null && rhs != null ) {
			return (lhs.getId().equals( rhs.getId() ));
		}
		return false;
	}

	/**
		Returns price formatted according to currency precision.
	*/
	static String displayPrice( BigDecimal price ) 
	{
		return NumberFormat.getCurrencyInstance().format( price );
	}

	static Map<String, String> splitProperties( String rawProps )
	{
		Map<String,String> properties = new TreeMap<String,String>();

		// if empty string, get out right away
		if ( rawProps == null || rawProps.isEmpty() ) {
			return properties;
		}

		// properties look something like: kv=1;kv2=2;...kvN=N
		String[] kvlist = rawProps.split( Conf.PROPERTY_SEPARATOR );

		// for each discovered pair of key-value...
		for (String rawKv: kvlist)
		{
			String[] kv = rawKv.split( Conf.KEY_VALUE_SEPARATOR );
			if ( kv.length == 2 ) {
				properties.put( kv[0], kv[1] );
			}
		}
		return properties;
	}

	/**
		Writes Item properties to a Parcel.
	*/
	static void writePropertiesToParcel( Parcel out, Map<String, String> map )
	{
		out.writeInt( map.size() );
		for( String key : map.keySet() )
		{
			out.writeString( key );
			out.writeString( map.get(key) );
		}
	}

	/**
		Reads Item properties, from a Parcel.
	*/
	static Map<String, String> readPropertiesFromParcel( Parcel in, Map<String, String> map )
	{
		int size = in.readInt();
		for(int i = 0; i < size; i++)
		{
			String key = in.readString();
			String value = in.readString();
			map.put( key, value );
		}
		return map;
	}

	public static void disableButton(Button btn, int bgRes, int textColor)
	{
		btn.setEnabled( false );
		btn.setClickable( false );
		btn.setBackgroundResource( bgRes );
		btn.setTextColor( textColor );
	}

	public static void enableButton(Button btn, int bgRes, int textColor)
	{
		btn.setEnabled( true );
		btn.setClickable( true );
		btn.setBackgroundResource( bgRes );
		btn.setTextColor( textColor );
	}

	static public String prettyRelativeTime( long nowTime, long eventTime )
	{
		long timeDifferenceMilliseconds = nowTime - eventTime;
		long diffHours = timeDifferenceMilliseconds / (3600 * 1000);

		if (diffHours < 1) {
			return shortTimeFormatter_.format(new Date(eventTime));
		} else {
			return fullTimeFormatter_.format(new Date(eventTime));
		}
	}

	static public void setCustomFont( Context context, TextView label, String fontPath )
	{
		Typeface font = Typeface.createFromAsset( context.getAssets(), fontPath );
		label.setTypeface(font);
	}

	static private final SimpleDateFormat shortTimeFormatter_ = new SimpleDateFormat("h:mm a", Locale.getDefault());
	static private final SimpleDateFormat fullTimeFormatter_ = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
}
