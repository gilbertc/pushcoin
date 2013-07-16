package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.ArrayList;

public class ShoppingCategoryMenuFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Store activity's dispacher
		dispatchable_ = ((IDispatcher)getActivity()).getDispachable();

		ListView view = (ListView)inflater.inflate(R.layout.shopping_category_menu, container, false);

		ArrayList<IconLabelArrayAdapter.Entry> menuItems = 
			new ArrayList<IconLabelArrayAdapter.Entry>();

		// populate menu item(s)
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.coffee_cup, "Breakfast") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.fries, "Lunch") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.pizza, "Hot Dinner") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.sushi_maki, "Salads") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.cheese_cake, "Deserts") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.wine_glass, "Beverages") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.toast, "Breads") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.apple, "Fruits") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.babelfish, "Fish") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.soft_drink, "Soda") );
		menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.cup_cake, "Snacks") );

		model_ = new IconLabelArrayAdapter(getActivity(), R.layout.shopping_category_menu_row, R.id.shopping_category_menu_icon, R.id.shopping_category_menu_label, menuItems);
		view.setAdapter(model_);

		// install click-event listener
		view.setOnItemClickListener(new ListSelection());

		return view;
	}

	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Message m = dispatchable_.obtainMessage(1, position, 0);
			m.sendToTarget();
		}
	}

	private IconLabelArrayAdapter model_;
	private Handler dispatchable_;
}
