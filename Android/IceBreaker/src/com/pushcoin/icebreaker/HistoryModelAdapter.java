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
	public HistoryModelAdapter(Context context, Controller ctrl)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from( context );

		context_ = context;
		ctrl_ = ctrl;
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
			convertView = inflater_.inflate(R.layout.history_row, null);
			
			// Creates a ViewHolder and store references to the children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.counterparty = (TextView) convertView.findViewById(R.id.txn_counterparty_name);
			holder.amount = (TextView) convertView.findViewById(R.id.txn_amount);
			holder.date = (TextView) convertView.findViewById(R.id.txn_date);
			holder.time = (TextView) convertView.findViewById(R.id.txn_time);
			holder.deviceName = (TextView) convertView.findViewById(R.id.txn_device_name);
			convertView.setTag(holder);
		} 
		else
		{
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			holder = (ViewHolder) convertView.getTag();
		}

		PcosHelper.TransactionInfo txn = ctrl_.getTransaction( position );

		// Bind the row-data with the holder.
		holder.counterparty.setText( txn.counterParty );
		holder.amount.setText( PcosHelper.prettyAmount( txn.amount, txn.currency ) );
		PcosHelper.DateTimePair txnTime = PcosHelper.prettyTimeParts( context_, txn.txnTimeEpoch );
		holder.date.setText( txnTime.date );
		holder.time.setText( txnTime.time );
		holder.deviceName.setText( txn.deviceName );
		
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
		TextView date;
		TextView time;
		TextView deviceName;
	}

	private LayoutInflater inflater_;
	final private Context context_;
	final private Controller ctrl_;
}
