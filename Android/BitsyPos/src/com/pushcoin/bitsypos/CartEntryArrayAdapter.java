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
	public CartEntryArrayAdapter(Context context, int entryLayoutResourceId, int titleViewResourceId, int priceViewResourceId, Cart cart)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from( context );
		// Cart instance we are serving the view 
		cart_ = cart;

		// Resource IDs of views for a row, icon and label
		entryLayoutResourceId_ = entryLayoutResourceId;
		titleViewResourceId_ = titleViewResourceId;
		priceViewResourceId_ = priceViewResourceId;
	}

	public int getCount() 
	{
		return cart_.size();
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
		Items in cart are identified by position.
	 */
	public long getItemId(int position) 
	{
		return position;
	}

	/**
		Underlaying cart.
	*/
	public Cart getCart() {
		return cart_;
	}

	/**
		Notify (view) listeners cart content has changed.
	*/
	public void refreshView()
	{
		notifyDataSetChanged();
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
			holder.price = (TextView) convertView.findViewById(priceViewResourceId_);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		Item item = cart_.get( position );

		// Bind the data efficiently with the holder.
		holder.title.setText( item.getName() );
		// TODO: hardcoded price
		holder.price.setText( "$4.49" );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView title;
		TextView price;
	}

	private LayoutInflater inflater_;
	private Cart cart_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int entryLayoutResourceId_;

	// Icon and label resource IDs
	final private int titleViewResourceId_;
	final private int priceViewResourceId_;
}
