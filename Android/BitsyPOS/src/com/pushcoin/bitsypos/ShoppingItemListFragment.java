package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.GridView;
import java.util.ArrayList;

public class ShoppingItemListFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		GridView view = (GridView)inflater.inflate(R.layout.shopping_item_list, container, false);

		ArrayList<ItemSummaryArrayAdapter.Entry> items = 
			new ArrayList<ItemSummaryArrayAdapter.Entry>();

		// populate item(s)
		items.add( new ItemSummaryArrayAdapter.Entry(R.drawable.coffee_cup, "Coffee", "Maine Roasted", "$3.75", R.drawable.cart_empty) );
		items.add( new ItemSummaryArrayAdapter.Entry(R.drawable.coffee_cup, "Coffee", "Maine Roasted", "$3.75", R.drawable.cart_empty) );
		items.add( new ItemSummaryArrayAdapter.Entry(R.drawable.coffee_cup, "Coffee", "Maine Roasted", "$3.75", R.drawable.cart_empty) );

		model_ = new ItemSummaryArrayAdapter(getActivity(), R.layout.shopping_item_list_block, R.id.shopping_item_list_product, R.id.shopping_item_list_title, R.id.shopping_item_list_desc, R.id.shopping_item_list_price, R.id.shopping_item_list_indicator, items);
		view.setAdapter(model_);

		return view;
	}

	private ItemSummaryArrayAdapter model_;
}
