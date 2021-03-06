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
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import java.util.Map;
import java.util.List;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import static java.util.Collections.unmodifiableList;

public class EditItemPropertiesAdapter extends BaseAdapter 
{
	public EditItemPropertiesAdapter(Context context, Map<String,String> properties)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Use ordered-container for easy mapping of grid-row with data
		properties_ = new ArrayList<Map.Entry<String,String>>( properties.entrySet() );
		// Sort properties alphabetically
		Collections.sort( properties_, new Comparator<Map.Entry<String,String>>()
			{
				@Override
				public int compare(Map.Entry<String,String> lhs, Map.Entry<String,String> rhs) {
					return lhs.getKey().compareTo(rhs.getKey());
				}
			});
	}

	public int getCount() 
	{
		return properties_.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) 
		{
			convertView = inflater_.inflate(R.layout.edit_item_properties_cell, null);

			// Creates a ViewHolder to store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.property = (CheckBox) convertView.findViewById(R.id.edit_item_property_editor);
			holder.property.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						Map.Entry<String,String> entry = properties_.get(position);
						properties_.set( position, 
							new AbstractMap.SimpleImmutableEntry<String,String>(
								entry.getKey(), isChecked ? Conf.PROPERTY_BOOL_TRUE : Conf.PROPERTY_BOOL_FALSE) );
					}
				});

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Map.Entry<String,String> property = properties_.get( position );
		holder.property.setText( property.getKey() );
		holder.property.setChecked( property.getValue().equals( Conf.PROPERTY_BOOL_TRUE ) );

		return convertView;
	}

	public List<Map.Entry<String,String>> getProperties() {
		return unmodifiableList( properties_ );
	}

	private static class ViewHolder 
	{
		CheckBox property;
	}

	private LayoutInflater inflater_;
	private ArrayList<Map.Entry<String,String>> properties_;
}
