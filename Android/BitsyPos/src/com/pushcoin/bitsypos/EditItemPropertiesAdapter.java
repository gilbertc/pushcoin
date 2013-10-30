package com.pushcoin.bitsypos;

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
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

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
			convertView = inflater_.inflate(R.layout.edit_item_properties_cell, null);

			// Creates a ViewHolder and store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.property = (CheckBox) convertView.findViewById(R.id.edit_item_property_editor);
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

	private static class ViewHolder 
	{
		CheckBox property;
	}

	private LayoutInflater inflater_;
	private ArrayList<Map.Entry<String,String>> properties_;
}
