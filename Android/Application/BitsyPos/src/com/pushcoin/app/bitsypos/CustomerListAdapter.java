package com.pushcoin.app.bitsypos;

import com.pushcoin.ifce.connect.data.Customer;

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

public class CustomerListAdapter extends BaseAdapter 
{
	public CustomerListAdapter(Context context)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);
		entries_ = Conf.EMPTY_CUSTOMER_LIST;
	}

	/**
		Go out and fetch categories.
	*/
	public void showData( List<Customer> data )
	{
		entries_ = data;
		// Tell view that underlaying content has changed.
		notifyDataSetChanged();
	}

	public int getCount() 
	{
		return entries_.size();
	}

	public Object getItem(int position) 
	{
		return entries_.get( position );
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
			convertView = inflater_.inflate(R.layout.customer_list_row, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.firstName = (TextView) convertView.findViewById(R.id.customer_list_fname);
			holder.lastName = (TextView) convertView.findViewById(R.id.customer_list_lname);
			holder.title = (TextView) convertView.findViewById(R.id.customer_list_title);
			holder.identifier = (TextView) convertView.findViewById(R.id.customer_list_identifier);
			holder.mugshot = (ImageView) convertView.findViewById(R.id.customer_list_mugshot);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind data with the holder.
		Customer user = entries_.get( position );
		holder.firstName.setText( user.firstName );
		holder.lastName.setText( user.lastName );
		holder.title.setText( user.title );
		holder.identifier.setText( user.identifier );
		holder.mugshot.setImageBitmap( user.mugshot );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView firstName;	
		TextView lastName;
		TextView title;
		TextView identifier;
		ImageView mugshot;
	}

	private LayoutInflater inflater_;
	private List<Customer> entries_;
}
