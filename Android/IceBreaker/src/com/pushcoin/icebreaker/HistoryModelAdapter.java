package com.pushcoin.icebreaker;

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

public class HistoryModelAdapter extends BaseAdapter 
{
	public HistoryModelAdapter(Context context, int entryLayoutResourceId, int counterpartyRscId, int amountRscId, int timeRscId, Controller ctrl)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from( context );
		// Cart instance we are serving the view 
		ctrl_ = ctrl;

		// Resource IDs of views for the row
		entryLayoutResourceId_ = entryLayoutResourceId;
		counterpartyRscId_ = counterpartyRscId;
		amountRscId_ = amountRscId;
		timeRscId_ = timeRscId;
	}

	public int getCount() 
	{
		return ctrl_.getHistorySize();
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
		Items in history are identified by position.
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
		ViewHolder holder;
		// First time around, just blindly inflate
		if (convertView == null) 
		{
			convertView = inflater_.inflate(entryLayoutResourceId_, null);
			
			// Creates a ViewHolder and store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.counterparty = (TextView) convertView.findViewById(counterpartyRscId_);
			holder.amount = (TextView) convertView.findViewById(amountRscId_);
			holder.time = (TextView) convertView.findViewById(timeRscId_);
			convertView.setTag(holder);
		} 
		else
		{
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			holder = (ViewHolder) convertView.getTag();
		}

		TransactionRecord txn = ctrl_.getTransaction( position );

		// Bind the row-data with the holder.
		holder.counterparty.setText( txn.counterparty );
		holder.amount.setText( txn.amount );
		holder.time.setText( txn.utctime );
		
		// alternate colors
		if ((position % 2) == 0) {
			convertView.setBackgroundResource(R.color.row_even);  
		} else {
			convertView.setBackgroundResource(R.color.row_uneven);  
		}

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView counterparty;
		TextView amount;
		TextView time;
	}

	private LayoutInflater inflater_;
	private Controller ctrl_;

	// The resource IDs
	final private int entryLayoutResourceId_;
	final private int counterpartyRscId_;
	final private int amountRscId_;
	final private int timeRscId_;
}
