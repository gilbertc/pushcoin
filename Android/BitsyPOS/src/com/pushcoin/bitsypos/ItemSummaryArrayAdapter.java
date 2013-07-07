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

public class ItemSummaryArrayAdapter extends BaseAdapter 
{
	public static class Entry
	{
		Entry(int pd, String ti, String de, String pr, int in)
		{
			product = pd;
			title = ti;
			desc = de;
			price = pr;
			indicator = in;
		}

		// The resource id of the product image data
		final int product;

		final String title;
		final String desc;
		final String price;

		// The resource id of the product indicator
		final int indicator;
	}

	public ItemSummaryArrayAdapter(Context context, int blockLayoutResourceId, int productViewResourceId, int titleViewResourceId, int descViewResourceId, int priceViewResourceId, int indicatorViewResourceId, Collection<Entry> entries)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs of views for a row, icon and label
		blockLayoutResourceId_ = blockLayoutResourceId;
		productViewResourceId_ = productViewResourceId;
		titleViewResourceId_ = titleViewResourceId;
		descViewResourceId_ = descViewResourceId;
		priceViewResourceId_ = priceViewResourceId;
		indicatorViewResourceId_ = indicatorViewResourceId;

		// Decode icon resource IDs to speed up drawing on scroll.
		entries_ = new ArrayList<CachedEntry>();
		for ( Entry e : entries )
		{
			CachedEntry ce = new CachedEntry();
			ce.product = BitmapFactory.decodeResource( context.getResources(), e.product );
			ce.title = e.title;
			ce.desc = e.desc;
			ce.price = e.price;
			ce.indicator = BitmapFactory.decodeResource( context.getResources(), e.indicator );
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
			holder.product = (ImageView) convertView.findViewById(productViewResourceId_);
			holder.title = (TextView) convertView.findViewById(titleViewResourceId_);
			holder.desc = (TextView) convertView.findViewById(descViewResourceId_);
			holder.price = (TextView) convertView.findViewById(priceViewResourceId_);
			holder.indicator = (ImageView) convertView.findViewById(indicatorViewResourceId_);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		holder.product.setImageBitmap( entries_.get( position ).product );
		holder.title.setText( entries_.get( position ).title );
		holder.desc.setText( entries_.get( position ).desc );
		holder.price.setText( entries_.get( position ).price );
		holder.indicator.setImageBitmap( entries_.get( position ).indicator );

		return convertView;
	}

	private static class ViewHolder 
	{
		ImageView product;
		TextView title;
		TextView desc;
		TextView price;
		ImageView indicator;
	}

	private static class CachedEntry
	{
		Bitmap product;
		String title;
		String desc;
		String price;
		Bitmap indicator;
	}

	private LayoutInflater inflater_;
	private List<CachedEntry> entries_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int blockLayoutResourceId_;

	// Icon and label resource IDs
	final private int productViewResourceId_;
	final private int titleViewResourceId_;
	final private int descViewResourceId_;
	final private int priceViewResourceId_;
	final private int indicatorViewResourceId_;
}
