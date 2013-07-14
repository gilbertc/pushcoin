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

public class CartEntryArrayAdapter extends BaseAdapter 
{
	public static class Entry
	{
		Entry(String ti, int qt, String pr, int st)
		{
			title = ti;
			qty = qt;
			price = pr;
			status = st;
		}

		final String title;
		final int qty;
		final String price;

		// The resource id of the status indicator
		final int status;
	}

	public CartEntryArrayAdapter(Context context, int entryLayoutResourceId, int titleViewResourceId, int qtyViewResourceId, int priceViewResourceId, int statusViewResourceId, Collection<Entry> entries)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs of views for a row, icon and label
		entryLayoutResourceId_ = entryLayoutResourceId;
		titleViewResourceId_ = titleViewResourceId;
		qtyViewResourceId_ = qtyViewResourceId;
		priceViewResourceId_ = priceViewResourceId;
		statusViewResourceId_ = statusViewResourceId;

		// Decode icon resource IDs to speed up drawing on scroll.
		entries_ = new ArrayList<CachedEntry>();
		for ( Entry e : entries )
		{
			CachedEntry ce = new CachedEntry();
			ce.title = e.title;
			ce.qty = e.qty;
			ce.price = e.price;
			ce.status = BitmapFactory.decodeResource( context.getResources(), e.status );
			entries_.add( ce );
		}
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
	 * Quick way to remove item from model
	 */
	public void remove(int position) 
	{
		// ignore if out of bounds
		if ( position < entries_.size() )
		{
			entries_.remove(position);
			notifyDataSetChanged();
		}
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
			convertView = inflater_.inflate(entryLayoutResourceId_, null);

			// Creates a ViewHolder and store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(titleViewResourceId_);
			holder.qty = (TextView) convertView.findViewById(qtyViewResourceId_);
			holder.price = (TextView) convertView.findViewById(priceViewResourceId_);
			holder.status = (ImageView) convertView.findViewById(statusViewResourceId_);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		holder.title.setText( entries_.get( position ).title );
		holder.qty.setText( "x " + Integer.toString(entries_.get( position ).qty) );
		holder.price.setText( entries_.get( position ).price );
		holder.status.setImageBitmap( entries_.get( position ).status );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView title;
		TextView qty;
		TextView price;
		ImageView status;
	}

	private static class CachedEntry
	{
		String title;
		int qty;
		String price;
		Bitmap status;
	}

	private LayoutInflater inflater_;
	private List<CachedEntry> entries_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int entryLayoutResourceId_;

	// Icon and label resource IDs
	final private int titleViewResourceId_;
	final private int qtyViewResourceId_;
	final private int priceViewResourceId_;
	final private int statusViewResourceId_;
}
