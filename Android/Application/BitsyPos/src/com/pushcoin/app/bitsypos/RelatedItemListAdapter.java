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
import java.util.Collection;
import java.util.List;

public class RelatedItemListAdapter extends BaseAdapter 
{
	public RelatedItemListAdapter(Context context, int rowLayoutResourceId, int labelViewResourceId, int priceViewResourceId, List<Item> entries)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs of views for a row, icon and label
		rowLayoutResourceId_ = rowLayoutResourceId;
		priceViewResourceId_ = priceViewResourceId;
		labelViewResourceId_ = labelViewResourceId;

		// Items themselves
		entries_ = entries;
	}

	public int getCount() 
	{
		return entries_.size();
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
			convertView = inflater_.inflate(rowLayoutResourceId_, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.label = (TextView) convertView.findViewById(labelViewResourceId_);
			holder.price = (TextView) convertView.findViewById(priceViewResourceId_);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		holder.label.setText( entries_.get( position ).getName() );
		holder.price.setText( Util.displayPrice( entries_.get( position ).getPrice() ) );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView label;
		TextView price;
	}

	private static class CachedEntry
	{
		// Image data
		Bitmap icon;

		// Label text
		String label;
	}

	private LayoutInflater inflater_;
	private List<Item> entries_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int rowLayoutResourceId_;

	// Icon and label resource IDs
	final private int priceViewResourceId_;
	final private int labelViewResourceId_;
}
