package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import de.timroes.swipetodismiss.SwipeDismissList;
import java.util.ArrayList;

public class ShoppingCartFragment extends Fragment 
{
	ArrayList<CartEntryArrayAdapter.Entry> items_ = 
		new ArrayList<CartEntryArrayAdapter.Entry>();

	CartEntryArrayAdapter adapter_;
	SwipeDismissList swipeList_;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// populate item(s)
		for (int i = 0; i < 10; ++i)
		{
			items_.add( new CartEntryArrayAdapter.Entry("Coffee, Maine Roasted", i+5, "$2.75", icons_[i%icons_.length]) );
		}

		adapter_ = new CartEntryArrayAdapter(getActivity(), R.layout.shopping_cart_row, 
			R.id.shopping_cart_entry_title,
			R.id.shopping_cart_entry_qty,
			R.id.shopping_cart_entry_price,
			R.id.shopping_cart_entry_status,
			items_);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);
		ListView cartItemList = (ListView) cartLayout.findViewById(R.id.shopping_cart_list);
		cartItemList.setAdapter(adapter_);

		SwipeDismissList.OnDismissCallback callback = 
			new SwipeDismissList.OnDismissCallback() {
				public SwipeDismissList.Undoable onDismiss(AbsListView listView, int position) 
				{		
					adapter_.remove( position );
					// final String itemToDelete = adapter_.getItem(position);
					// adapter_.remove(itemToDelete);
					return null;
				}
			};

		swipeList_ = new SwipeDismissList(cartItemList, callback, SwipeDismissList.UndoMode.SINGLE_UNDO);

		return cartLayout;
	}

	final int icons_[] = {
		R.drawable.mono_checkbox,
		R.drawable.mono_cooking,
		R.drawable.mono_fork_knife,
		R.drawable.mono_loop,
		R.drawable.mono_notepad,
		R.drawable.mono_warning,
	};
}
