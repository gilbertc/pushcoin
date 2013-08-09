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
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class CategoryListAdapter extends BaseAdapter 
{
	public static class Entry
	{
		Entry(int ic, String lb)
		{
			icon = ic;
			label = lb;
		}

		// The resource id of the image data
		final int icon;

		// Label text
		final String label;
	}

	public CategoryListAdapter(Context context, int rowLayoutResourceId, int iconViewResourceId, int labelViewResourceId)
	{
		ctx_ = context;
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs of views for a row, icon and label
		rowLayoutResourceId_ = rowLayoutResourceId;
		iconViewResourceId_ = iconViewResourceId;
		labelViewResourceId_ = labelViewResourceId;

		// Try loading data.
		reloadData();
	}

	/**
		Go out and fetch categories.
	*/
	public void reloadData()
	{
		// Decode icon resource IDs to speed up drawing on scroll.
		entries_ = new ArrayList<CachedEntry>();
		for ( Category cat : AppDb.getInstance(ctx_).getMainCategories() )
		{
			CachedEntry ce = new CachedEntry();
			ce.icon = BitmapFactory.decodeResource( ctx_.getResources(), R.drawable.cheese_cake );
			ce.label = cat.category_id;
			ce.tag_id = cat.tag_id;
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
			holder.icon = (ImageView) convertView.findViewById(iconViewResourceId_);
			holder.label = (TextView) convertView.findViewById(labelViewResourceId_);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		holder.icon.setImageBitmap( entries_.get( position ).icon );
		holder.label.setText( entries_.get( position ).label );

		return convertView;
	}

	private static class ViewHolder 
	{
		ImageView icon;
		TextView label;
	}

	private static class CachedEntry
	{
		// Image data
		Bitmap icon;

		// Label text
		String label;

		// Tag ID
		String tag_id;
	}

	private Context ctx_;
	private LayoutInflater inflater_;
	private List<CachedEntry> entries_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int rowLayoutResourceId_;

	// Icon and label resource IDs
	final private int iconViewResourceId_;
	final private int labelViewResourceId_;
}
