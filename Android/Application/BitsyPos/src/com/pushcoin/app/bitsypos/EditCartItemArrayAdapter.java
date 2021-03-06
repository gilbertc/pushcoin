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

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.AbstractList;
import java.util.List;
import java.text.NumberFormat;
import java.math.BigDecimal;

public class EditCartItemArrayAdapter extends BaseAdapter
{
	// When we used DataSetObserver, we saw rendering problems, presumably because
	// our EditText is embedded into a ListView. So, instead we now have own
	// handler interface.
	interface OnContentChanged
	{
		void onChanged();
		void onInputError();
	}

	public EditCartItemArrayAdapter(Context context, OnContentChanged handler, Cart.Combo combo)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);
		handler_ = handler;
		combo_ = combo;
	}

	public int getCount() 
	{
		return combo_.entries.size();
	}

	/**
	 * Since the data comes from an array, just returning the index is
	 * sufficent to get at the data. If we were using a more complex data
	 * structure, we would return whatever object represents one row in the
	 * list.
	 *
	 */
	public Object getItem(int position) 
	{
		return position;
	}

	/**
	 * Use the array index as a unique id.
	 */
	public long getItemId(int position) 
	{
		return position;
	}

	/**
	 * Make a view to hold each row.
	 */
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) 
		{
			convertView = inflater_.inflate(R.layout.edit_cart_item_row_changing, null);

			// Creates a ViewHolder and store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.sku = (EditText) convertView.findViewById(R.id.edit_cart_item_row_sku);
			holder.desc = (EditText) convertView.findViewById(R.id.edit_cart_item_row_desc);
			holder.qty = (EditText) convertView.findViewById(R.id.edit_cart_item_row_qty);
			holder.price = (EditText) convertView.findViewById(R.id.edit_cart_item_row_price);
			
			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Cart.Entry cartEntry = combo_.entries.get( position );
		holder.sku.setText( cartEntry.sku );
		holder.sku.setId(position);
		holder.desc.setText( cartEntry.name );
		holder.desc.setId(position);
		holder.qty.setText( Integer.toString(cartEntry.qty) );
		holder.qty.setId(position);
		holder.price.setText( NumberFormat.getCurrencyInstance().format( cartEntry.unitPrice ) );
		holder.price.setId(position);

		// register change listener -- one for each member
		// SKU
		holder.sku.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				EditText field = (EditText) v;
				Cart.Entry cartEntry = combo_.entries.get( v.getId() );

				if (!hasFocus) 
				{
					// Update only if changed
					String newVal = field.getText().toString();
					if ( !newVal.equals( cartEntry.sku )) 
					{
						cartEntry.sku = newVal;
						handler_.onChanged();
					}
				}
			}
		});

		// Description
		holder.desc.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				EditText field = (EditText) v;
				Cart.Entry cartEntry = combo_.entries.get( v.getId() );

				if (!hasFocus) 
				{
					// Update only if changed
					String newVal = field.getText().toString();
					if ( !newVal.equals( cartEntry.name )) 
					{
						cartEntry.name = newVal;
						handler_.onChanged();
					}
				}
			}
		});

		// Qty
		holder.qty.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				EditText field = (EditText) v;
				Cart.Entry cartEntry = combo_.entries.get( v.getId() );

				if (!hasFocus) 
				{
					// Update only if changed
					String newVal = field.getText().toString();
					if ( !newVal.isEmpty() && !newVal.equals( Integer.toString(cartEntry.qty) )) 
					{
						try
						{
							int newQty = Integer.parseInt( newVal );
							if (newQty < 1) {
								combo_.entries.remove( v.getId() );
							} 
							else {
								cartEntry.qty = newQty;
							}
							handler_.onChanged();
						} catch (NumberFormatException e) { 
							handler_.onInputError();
						}
					} 
					field.setText( Integer.toString(cartEntry.qty) );
				} 
				else {
					field.setText("");
				}
			}
		});

		// this prevents the field from regaining focus after it's edited
		holder.qty.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if ( actionId == EditorInfo.IME_ACTION_DONE )
				{
					v.clearFocus();
					handler_.onChanged();
				}
				return false;
			}
		});

		// Price
		holder.price.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				EditText field = (EditText) v;
				Cart.Entry cartEntry = combo_.entries.get( v.getId() );
				if (!hasFocus) 
				{
					// Update only if changed
					String newVal = field.getText().toString();
					if (!newVal.isEmpty() && !newVal.equals( cartEntry.unitPrice.toString() )) 
					{
						try
						{
							cartEntry.unitPrice = new BigDecimal( newVal );
							handler_.onChanged();
						} catch (NumberFormatException e) { 
							handler_.onInputError();
						}
					} 
					field.setText( NumberFormat.getCurrencyInstance().format( cartEntry.unitPrice ) );
				} 
				else {
					field.setText("");
				}
			}
		});

		// this prevents the field from regaining focus after it's edited
		holder.price.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if ( actionId == EditorInfo.IME_ACTION_DONE )
				{
					v.clearFocus();
					handler_.onChanged();
				}
				return false;
			}
		});

		return convertView;
	}

	private static class ViewHolder 
	{
		EditText sku;
		EditText desc;
		EditText qty;
		EditText price;
	}

	private final LayoutInflater inflater_;
	private final OnContentChanged handler_;
	private final Cart.Combo combo_;
}
