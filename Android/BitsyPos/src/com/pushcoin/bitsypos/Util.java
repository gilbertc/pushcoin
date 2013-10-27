package com.pushcoin.bitsypos;

import java.math.BigDecimal;
import java.text.NumberFormat;
import android.database.Cursor;
import java.util.Map;
import java.util.TreeMap;

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
}
