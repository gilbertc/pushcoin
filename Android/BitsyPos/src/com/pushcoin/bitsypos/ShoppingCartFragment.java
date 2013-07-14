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
	ArrayList<String> numbers_ = new ArrayList<String>();
	ArrayAdapter<String> adapter_;
	SwipeDismissList swipeList_;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// cart data
		numbers_.add("one");
		numbers_.add("two");
		numbers_.add("three");
		numbers_.add("four");
		numbers_.add("five");
		numbers_.add("six");
		numbers_.add("seven");
		numbers_.add("eight");
		numbers_.add("nine");
		numbers_.add("ten");
		numbers_.add("eleven");
		numbers_.add("twelve");
		numbers_.add("thirteen");
		numbers_.add("fourteen");
		numbers_.add("fifteen");
		numbers_.add("sixteen");
		numbers_.add("seventeen");
		numbers_.add("eighteen");
		numbers_.add("nineteen");
		numbers_.add("twenty");
		numbers_.add("twenty one");
		numbers_.add("twenty two");
		
		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);
		ListView cartItemList = (ListView) cartLayout.findViewById(R.id.shopping_cart_list);

		SwipeDismissList.OnDismissCallback callback = 
			new SwipeDismissList.OnDismissCallback() {
				public SwipeDismissList.Undoable onDismiss(AbsListView listView, int position) 
				{		
					// if removing too fast, we may refer to wrong position
					if ( position < numbers_.size() )
					{
						final String itemToDelete = adapter_.getItem(position);
						adapter_.remove(itemToDelete);
					}
					return null;
				}
			};

		swipeList_ = new SwipeDismissList(cartItemList, callback, SwipeDismissList.UndoMode.SINGLE_UNDO);
		adapter_ = new ArrayAdapter<String>(getActivity(), R.layout.shopping_cart_row, R.id.shopping_cart_item, numbers_);
		cartItemList.setAdapter(adapter_);

		return cartLayout;
	}
}
