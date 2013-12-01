/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.app.bitsypos;

import com.pushcoin.ifce.connect.data.Customer;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.LayoutInflater;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.text.NumberFormat;

public class TransactionListAdapter extends BaseAdapter 
{
	public TransactionListAdapter(Context context)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		deniedColor_ = context.getResources().getColor(R.color.android_holo_red_bright);
		approvedColor_ = context.getResources().getColor(R.color.android_holo_green_light);
		pendingColor_ = context.getResources().getColor(R.color.android_holo_yellow_bright);

		// Try loading data.
		reloadData();
	}

	/**
		Go out and fetch categories.
	*/
	public void reloadData()
	{
		// Fetch active cart, which stores transactions related to it.
		cart_ = CartManager.getInstance().getActiveCart();

		// Tell view that underlaying content has changed.
		notifyDataSetChanged();
	}

	@Override
	public int getCount() 
	{
		return cart_.totalTransactions();
	}

	@Override
	public Object getItem(int position) 
	{
		return cart_.getTransaction( position );
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
			convertView = inflater_.inflate(R.layout.transaction_list_row, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.amount = (TextView) convertView.findViewById(R.id.transaction_list_amount);
			holder.status = (TextView) convertView.findViewById(R.id.transaction_list_status);
			holder.createTime = (TextView) convertView.findViewById(R.id.transaction_list_timestamp);
			holder.customerName = (TextView) convertView.findViewById(R.id.transaction_list_customer_name);
			holder.customerAccount = (TextView) convertView.findViewById(R.id.transaction_list_customer_account);

			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the UI views
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data
		Transaction tx = cart_.getTransaction( position );
		holder.amount.setText( NumberFormat.getCurrencyInstance().format( tx.getAmount()));
		holder.status.setText( tx.getPrettyStatus() );

		// Apply color to status
		deriveColorFromStatus( holder.status, tx.getStatus() );

		// Set animation if pending
		blinkFromStatus( holder.status, tx.getStatus() );

		holder.createTime.setText( Util.prettyRelativeTime( System.currentTimeMillis(), tx.getCreateTime()) );

		// attache customer info if present
		Customer customer = tx.getCustomer();
		if (customer != null)
		{
			holder.customerName.setText( customer.firstName + " " + customer.lastName );
			holder.customerAccount.setText( customer.identifier );
			holder.customerName.setVisibility( View.VISIBLE );
			holder.customerAccount.setVisibility( View.VISIBLE );
		} 
		else
		{
			holder.customerName.setVisibility( View.GONE );
			holder.customerAccount.setVisibility( View.GONE );
		}

		return convertView;
	}

	private void blinkFromStatus( TextView label, int txStatus )
	{
		if (txStatus == Transaction.STATUS_PENDING)
		{
			Animation anim = new AlphaAnimation(0.0f, 1.0f);
			anim.setDuration(500); //You can manage the time of the blink with this parameter
			anim.setStartOffset(20);
			anim.setRepeatMode(Animation.REVERSE);
			anim.setRepeatCount(Animation.INFINITE);
			label.startAnimation(anim);
		} else {
			label.clearAnimation();
		}
	}

	private void deriveColorFromStatus( TextView label, int txStatus )
	{
		int color;
		switch (txStatus)
		{
			case Transaction.STATUS_APPROVED:
				color = approvedColor_;
				break;

			case Transaction.STATUS_DENIED:
				color = deniedColor_;
				break;

			default: color = pendingColor_;
		}
		label.setTextColor( color );
	}

	private static class ViewHolder 
	{
		TextView amount;
		TextView status;
		TextView createTime;
		TextView customerName;
		TextView customerAccount;
	}

	private LayoutInflater inflater_;
	private Cart cart_;
	private int deniedColor_;
	private int approvedColor_;
	private int pendingColor_;
}
