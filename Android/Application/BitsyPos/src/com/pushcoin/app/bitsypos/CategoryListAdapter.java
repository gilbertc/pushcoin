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
import java.util.ArrayList;

public class CategoryListAdapter extends BaseAdapter 
{
	public static class Entry
	{
		// Holds ref to a category this entry describes
		Category cat;

		// We change the look of actively selected category
		boolean isActive;
	}

	public CategoryListAdapter(Context context, int rowLayoutResourceId, int labelViewResourceId)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs of views for a row, icon and label
		rowLayoutResourceId_ = rowLayoutResourceId;
		labelViewResourceId_ = labelViewResourceId;

		// cache few color resources
		btnTextColorOn_ = context.getResources().getColor( R.color.android_holo_blue_bright );
		btnTextColorOff_ = context.getResources().getColor( R.color.lightui_mediumgray );

		// Try loading data.
		reloadData();
	}

	/**
		Go out and fetch categories.
	*/
	public void setActiveEntry( int position )
	{
		// clear previous active
		Entry old = getActiveEntry();
		if (old != null) {
			old.isActive = false;
		}

		// if provided, set the new active
		if (position != NONE_ACTIVE) {
			entries_.get( position ).isActive = true;
		}

		// Tell view the underlaying content has changed.
		notifyDataSetChanged();
	}

	public Entry getActiveEntry()
	{
		int position = getActiveEntryPosition();
		return (position != NONE_ACTIVE) ? entries_.get( position ) : null;
	}

	public int getActiveEntryPosition()
	{
		for (int i = 0; i < entries_.size(); ++i)
		{
			Entry e = entries_.get(i);	
			if (e.isActive) {
				return i;
			}
		}
		return NONE_ACTIVE;
	}

	/**
		Go out and fetch categories.
	*/
	public void reloadData()
	{
		// We wrap categories into our Entry-holder, so we can track
		// which category is currently active and render the label 
		// slightly differently.
		entries_ = new ArrayList<Entry>();
		for ( Category cat : AppDb.getInstance().getMainCategories() )
		{
			Entry ce = new Entry();
			ce.cat = cat;
			ce.isActive = false;
			entries_.add( ce );
		}

		// Tell view that underlaying content has changed.
		notifyDataSetChanged();
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
	 * Use the array index to lookup entry.
	 */
	public Entry getEntry(int position) 
	{
		return entries_.get(position);
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

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Depending if entry is active, we show slightly different view
		Entry en = entries_.get( position );
		if ( en.isActive ) {
			holder.label.setTextColor( btnTextColorOn_ );
		} else { // not active
			holder.label.setTextColor( btnTextColorOff_ );
		}

		// Bind the data
		holder.label.setText( en.cat.label );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView label;
	}

	private LayoutInflater inflater_;
	private List<Entry> entries_;
	private int btnTextColorOn_;
	private int btnTextColorOff_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int rowLayoutResourceId_;

	// Icon and label resource IDs
	final private int labelViewResourceId_;

	static public final int NONE_ACTIVE = -1;
}
