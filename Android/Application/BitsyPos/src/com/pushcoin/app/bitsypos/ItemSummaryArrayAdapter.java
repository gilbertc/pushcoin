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
import java.util.List;

public class ItemSummaryArrayAdapter extends BaseAdapter 
{
	public ItemSummaryArrayAdapter(Context context, int blockLayoutResourceId, List<Item> entries)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs of views for a row, icon and label
		blockLayoutResourceId_ = blockLayoutResourceId;

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
			convertView = inflater_.inflate(blockLayoutResourceId_, null);

			// Creates a ViewHolder and store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.item_summary_title);
			holder.price = (TextView) convertView.findViewById(R.id.item_summary_price);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Item item = entries_.get( position );
		holder.title.setText( item.getName() );
		holder.price.setText( item.isDefined() ? Util.displayPrice( item.getPrice() ) : "..." );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView title;
		TextView price;
	}

	private LayoutInflater inflater_;
	private List<Item> entries_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int blockLayoutResourceId_;
}