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
	public CartEntryArrayAdapter(Context context, int rowRes, int titleRes, int priceRes, int noteRes)
	{
		rowRes_ = rowRes;
		titleRes_ = titleRes; 
		priceRes_ = priceRes;
		noteRes_ = noteRes;
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
		View view = inflater_.inflate(rowRes_, null);
		
		// Creates a ViewHolder and store references to the children views
		// we want to bind data to.
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView) view.findViewById(titleRes_);
		holder.price = (TextView) view.findViewById(priceRes_);
		holder.note = (TextView) view.findViewById(noteRes_);
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
		// show note only if present
		if ( item.note.isEmpty() ) {
			holder.note.setVisibility( View.GONE );
		} else {
			holder.note.setVisibility( View.VISIBLE );
		}
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

	final int rowRes_;
	final int titleRes_;
	final int priceRes_;
	final int noteRes_;
	private LayoutInflater inflater_;
	private int convertViewVer_ = 0;
}
