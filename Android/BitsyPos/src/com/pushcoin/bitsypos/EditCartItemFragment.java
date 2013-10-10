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
		final Cart cart = (Cart) access_.session( Conf.SESSION_CART );

		// Locate cart item we are modifying and make a clone of it
		cartItemId_ = getArguments().getInt( Conf.FIELD_CART_ITEM_POSITION );
		combo_ = new Cart.Combo( cart.get( cartItemId_ ) );

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.edit_cart_item_view, container, false);

		// References to widgets...
		comboName_ = (EditText) view.findViewById(R.id.edit_cart_item_view_name);

		// Find the listview widget so we can set its adapter
		ListView itemsListView = (ListView) view.findViewById(R.id.edit_cart_item_view_list);
		itemsListView.addHeaderView( inflater.inflate(R.layout.edit_cart_item_row_header, null) );

		listViewAdapter_ = new EditCartItemArrayAdapter( context, combo_ );
		itemsListView.setAdapter( listViewAdapter_ );

		// install click-event listener
		itemsListView.setOnItemClickListener(new EditCartItemListener());

		// update view
		onModelUpdated( true );

		return view;
	}

	private void onModelUpdated( boolean init )
	{
		comboName_.setText( combo_.getName() );

		// no need to re-update listview on startup
		if (! init ) {
			listViewAdapter_.notifyDataSetChanged();
		}
	}

	private class EditCartItemListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Log.v(Conf.TAG, "cart-item-edit="+position );
		}
	}

	private SessionManager access_;

	// Model
	private int cartItemId_;
	private Cart.Combo combo_;

	// View widgets
	EditText comboName_;
	EditCartItemArrayAdapter listViewAdapter_;
}
