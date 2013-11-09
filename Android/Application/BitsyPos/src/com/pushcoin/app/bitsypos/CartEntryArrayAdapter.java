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

public class CartEntryArrayAdapter extends BaseAdapter 
{
	public CartEntryArrayAdapter(Context context)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from( context );
	}

	public int getCount() 
	{
		return CartManager.getInstance().getActiveCart().size();
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
		View view = inflater_.inflate(R.layout.shopping_cart_row, null);
		
		// Creates a ViewHolder and store references to the children views
		// we want to bind data to.
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView) view.findViewById(R.id.shopping_cart_entry_title);
		holder.price = (TextView) view.findViewById(R.id.shopping_cart_entry_price);
		holder.note = (TextView) view.findViewById(R.id.shopping_cart_entry_note);
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
		Cart.Combo item = CartManager.getInstance().getActiveCart().get( position );

		// Bind the row-data with the holder.
		String label = item.getName();
		
		// If item has slots, append the name of each.
		if (item.entries.size() > 1)	
		{
			label += ": ";
			for (Cart.Entry entry: item.entries) {
				label += "\n"+Integer.toString(entry.qty)+"x "+entry.name;
			}
		}

		holder.title.setText( label );
		holder.price.setText( item.getPrettyPrice() );
		holder.note.setText( item.note );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView title;
		TextView price;
		TextView note;
		int convertViewVer;
	}

	private LayoutInflater inflater_;
	private int convertViewVer_ = 0;
}
