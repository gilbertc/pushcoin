package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.GridView;
import android.widget.ArrayAdapter;

public class ShoppingCartFragment extends Fragment 
{
	static final String[] numbers = new String[] {
					"one", "two", "three", "four", "five",
					"six", "seven", "eight", "nine", "ten",
					"eleven", "twelve", "thirteen", "fourteen",
					"fifteen","sixteen","seventeen","eighteen",
					"nineteen","twenty","twenty one","twenty two"
					};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);
		GridView cartItemList = (GridView) cartLayout.findViewById(R.id.shopping_cart_list);

		ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, numbers);
		cartItemList.setAdapter(adapter);

		return cartLayout;
	}
}
