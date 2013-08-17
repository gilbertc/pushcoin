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

	public void remove(int position)
	{
		if ( cart_.remove(position) != null ) {
			refreshView();
		}
	};

	public void insert(Item item, int position)
	{
		cart_.insert(item, position);
		refreshView();
	};

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
		// invalidate convertViews
		++convertViewVer_;
		notifyDataSetChanged();
	}

	private View inflateRow()
	{
		View view = inflater_.inflate(entryLayoutResourceId_, null);
		
		// Creates a ViewHolder and store references to the children views
		// we want to bind data to.
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView) view.findViewById(titleViewResourceId_);
		holder.price = (TextView) view.findViewById(priceViewResourceId_);
		holder.convertViewVer = convertViewVer_;
		view.setTag(holder);
		return view;
	}

	/**
	 * Make a view to hold each row.
	 */
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// First time around, just blindly inflate
		if (convertView == null) {
			convertView = inflateRow();
		} 
		else
		{
			// When convertView is not null, /may be/ we can reuse it directly,
			if (((ViewHolder) convertView.getTag()).convertViewVer != convertViewVer_) {
				convertView = inflateRow();
			}
		}

		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder = (ViewHolder) convertView.getTag();
		Item item = cart_.get( position );

		// Bind the data efficiently with the holder.
		holder.title.setText( item.getName() );
		holder.price.setText( item.getPrettyPrice(Conf.FIELD_PRICE_TAG_DEFAULT) );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView title;
		TextView price;
		int convertViewVer;
	}

	private LayoutInflater inflater_;
	private Cart cart_;
	private int convertViewVer_ = 0;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int entryLayoutResourceId_;

	// Icon and label resource IDs
	final private int titleViewResourceId_;
	final private int priceViewResourceId_;
}
