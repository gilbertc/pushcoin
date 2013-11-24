package com.pushcoin.app.bitsypos;

import android.widget.BaseAdapter;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.util.Log;
import java.util.Locale;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class TabMenuAdapter extends BaseAdapter 
{
	public TabMenuAdapter(Context context)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);

		activeLabelColor_ = context.getResources().getColor(R.color.lightui_darkestgray);
		activeLabelBgResource_ = R.color.lightui_lightestgray;

		inactiveLabelColor_ = context.getResources().getColor(R.color.lightui_darkgray);
		inactiveLabelBgResource_ = android.R.color.white;
	}

	// Returns number of tabs we manage
	public int getCount()
	{
		return CartManager.getInstance().size();
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
		return CartManager.getInstance().get( position );
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
			convertView = inflater_.inflate(R.layout.tab_menu_row, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.label = (TextView) convertView.findViewById(R.id.tab_menu_label);
			holder.createTime = (TextView) convertView.findViewById(R.id.tab_menu_create_time);
			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind data with the holder.
		CartManager.Entry entry = CartManager.getInstance().get( position );
		Log.v( Conf.TAG, "tab-entry="+ entry.name + ";is-active="+entry.active);
		if (entry.active)
		{
			convertView.setBackgroundResource( activeLabelBgResource_ );
			holder.label.setTextColor( activeLabelColor_ );
			holder.createTime.setTextColor( activeLabelColor_ );
		}
		else
		{
			convertView.setBackgroundResource( inactiveLabelBgResource_ );
			holder.label.setTextColor( inactiveLabelColor_ );
			holder.createTime.setTextColor( inactiveLabelColor_ );
		}
		holder.label.setText( entry.name );
		holder.createTime.setText( timeFormatter_.format( entry.tmCreate ) );

		return convertView;
	}

	private static class ViewHolder 
	{
		TextView label;
		TextView createTime;
	}

	private LayoutInflater inflater_;
	private int activeLabelColor_;
	private int activeLabelBgResource_;
	private int inactiveLabelColor_;
	private int inactiveLabelBgResource_;

	static private final SimpleDateFormat timeFormatter_ = new SimpleDateFormat("h:mm a", Locale.getDefault());
}
