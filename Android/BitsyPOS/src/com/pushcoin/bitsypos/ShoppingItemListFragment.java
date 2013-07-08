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
		for (int i = 0; i < 100; ++i)
		{
			items.add( new ItemSummaryArrayAdapter.Entry(icons_[i%icons_.length], "Coffee", "Maine Roasted", "$3.75", R.drawable.cart_empty_gray) );
		}

		model_ = new ItemSummaryArrayAdapter(getActivity(), R.layout.shopping_item_list_block, R.id.shopping_item_list_product, R.id.shopping_item_list_title, R.id.shopping_item_list_desc, R.id.shopping_item_list_price, R.id.shopping_item_list_indicator, items);
		view.setAdapter(model_);

		return view;
	}

	final int icons_[] = {
		R.drawable.apple,
		R.drawable.babelfish,
		R.drawable.cake,
		R.drawable.cake2,
		R.drawable.cart_empty,
		R.drawable.cart_empty_gray,
		R.drawable.cheese_cake,
		R.drawable.coffee_cup,
		R.drawable.corbeille_box,
		R.drawable.cup_cake,
		R.drawable.fries,
		R.drawable.fruit_cake,
		R.drawable.ic_launcher,
		R.drawable.pizza,
		R.drawable.shopping_category_menu_row_selector,
		R.drawable.shopping_item_list_selector,
		R.drawable.soft_drink,
		R.drawable.sushi_maki,
		R.drawable.toast,
		R.drawable.wine_glass, 
	};

	private ItemSummaryArrayAdapter model_;
}
