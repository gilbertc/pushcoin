package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Fragment;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import java.util.ArrayList;

public class EditCartItemFragment extends DialogFragment 
{
	/**
		Create a new instance, providing cart item ID. 
	*/
	static EditCartItemFragment newInstance(int position)
	{
		EditCartItemFragment f = new EditCartItemFragment();

		// Supply position input as an argument.
		Bundle args = new Bundle();
		args.putInt(Conf.FIELD_CART_ITEM_POSITION, position);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_NoActionBar_Fullscreen);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context context = getActivity();

		// Obtain access to session manager, from which we get current cart
		access_ = SessionManager.getInstance( context );
		Cart cart = (Cart) access_.session( Conf.SESSION_CART );

		// Locate cart item we are modifying..
		int cartItemId = getArguments().getInt( Conf.FIELD_CART_ITEM_POSITION );
		Cart.Combo combo = cart.get( cartItemId );

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.edit_cart_item_view, container, false);

		// Allow user to change the title
		EditText comboName = (EditText) view.findViewById(R.id.edit_cart_item_view_name);
		comboName.setText( combo.getName() );

		// Find the listview widget so we can set its adapter
		ListView itemsListView = (ListView) view.findViewById(R.id.edit_cart_item_view_list);
		itemsListView.addHeaderView( inflater.inflate(R.layout.edit_cart_item_row_header, null) );

		EditCartItemArrayAdapter adapter = new EditCartItemArrayAdapter( context, combo.entries );
		itemsListView.setAdapter( adapter );

		// install click-event listener
		//itemsListView.setOnItemClickListener(new ListSelection());

		return view;
	}

	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Message m = dispatchable_.obtainMessage( MessageId.SHOPPING_ITEM_CLICKED, 0, 0, items_.get(position).getId() );
			m.sendToTarget();
		}
	}

	private SessionManager access_;
	private Handler dispatchable_;
	private ArrayList<Item> items_;
}
