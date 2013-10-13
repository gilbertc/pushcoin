package com.pushcoin.bitsypos;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.AbstractList;
import java.util.List;
import java.text.NumberFormat;
import java.math.BigDecimal;

public class EditCartItemArrayAdapter extends BaseAdapter 
{
	public EditCartItemArrayAdapter(Context context, Cart.Combo combo)
	{
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater_ = LayoutInflater.from(context);
		combo_ = combo;
	}

	public int getCount() 
	{
		return combo_.entries.size();
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
			holder.sku = (EditText) convertView.findViewById(skuViewResourceId_);
			holder.desc = (EditText) convertView.findViewById(descViewResourceId_);
			holder.qty = (EditText) convertView.findViewById(qtyViewResourceId_);
			holder.price = (EditText) convertView.findViewById(priceViewResourceId_);
			
			convertView.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Cart.Entry cartEntry = combo_.entries.get( position );
		holder.sku.setText( cartEntry.sku );
		holder.sku.setId(position);
		holder.desc.setText( cartEntry.name );
		holder.desc.setId(position);
		holder.qty.setText( Integer.toString(cartEntry.qty) );
		holder.qty.setId(position);
		holder.price.setText( cartEntry.unitPrice.toString() );
		holder.price.setId(position);

		// register change listener -- one for each member
		// SKU
		holder.sku.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				if (!hasFocus) {
					combo_.entries.get( v.getId() ).sku = ((TextView) v).getText().toString();
				}
			}
		});

		// Price
		holder.price.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				EditText field = (EditText) v;
				Cart.Entry cartEntry = combo_.entries.get( v.getId() );
				if (!hasFocus) 
				{
					// Editing finished. If empty, restore previous value.
					if (field.getText().length() > 0) {
						cartEntry.unitPrice = new BigDecimal( field.getText().toString() );
					} else {
						field.setText( cartEntry.unitPrice.toString() );
					}
				} 
			}
		});

		return convertView;
	}

	private static class ViewHolder 
	{
		EditText sku;
		EditText desc;
		EditText qty;
		EditText price;
	}

	private final LayoutInflater inflater_;
	private final Cart.Combo combo_;

	// resource IDs
	final private int blockLayoutResourceId_ = R.layout.edit_cart_item_row_changing;
	final private int skuViewResourceId_ = R.id.edit_cart_item_row_sku;
	final private int descViewResourceId_ = R.id.edit_cart_item_row_desc;
	final private int qtyViewResourceId_ = R.id.edit_cart_item_row_qty;
	final private int priceViewResourceId_ = R.id.edit_cart_item_row_price;
}
