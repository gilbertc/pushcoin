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
import java.util.AbstractList;
import java.util.List;
import java.text.NumberFormat;

public class EditCartItemArrayAdapter extends BaseAdapter 
{
	public EditCartItemArrayAdapter(Context context, AbstractList<Cart.Entry> entries)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		// Resource IDs
		blockLayoutResourceId_ = R.layout.edit_cart_item_row_readonly;
		skuViewResourceId_ = R.id.edit_cart_item_row_sku;
		descViewResourceId_ = R.id.edit_cart_item_row_desc;
		qtyViewResourceId_ = R.id.edit_cart_item_row_qty;
		priceViewResourceId_ = R.id.edit_cart_item_row_price;

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
			holder.sku = (TextView) convertView.findViewById(skuViewResourceId_);
			holder.desc = (TextView) convertView.findViewById(descViewResourceId_);
			holder.qty = (TextView) convertView.findViewById(qtyViewResourceId_);
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
		Cart.Entry cartEntry = entries_.get( position );
		holder.sku.setText( cartEntry.sku );
		holder.desc.setText( cartEntry.name );
		holder.qty.setText( Integer.toString(cartEntry.qty) );
		holder.price.setText( NumberFormat.getCurrencyInstance().format( cartEntry.unitPrice ) );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView sku;
		TextView desc;
		TextView qty;
		TextView price;
	}

	private LayoutInflater inflater_;
	private AbstractList<Cart.Entry> entries_;

	// The resource ID for a layout file containing a layout to use when instantiating views.
	final private int blockLayoutResourceId_;

	// resource IDs
	final private int skuViewResourceId_;
	final private int descViewResourceId_;
	final private int qtyViewResourceId_;
	final private int priceViewResourceId_;
}
