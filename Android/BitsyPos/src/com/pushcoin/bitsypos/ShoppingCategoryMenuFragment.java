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
		// Listview showing categories.
		View layout = inflater.inflate(R.layout.shopping_category_menu, container, false);
		ListView menu = (ListView)layout.findViewById( R.id.shopping_category_menu );

		model_ = new CategoryListAdapter(getActivity(), R.layout.shopping_category_menu_row, R.id.shopping_category_menu_icon, R.id.shopping_category_menu_label);
		menu.setAdapter(model_);

		// install click-event listener
		menu.setOnItemClickListener(new ListSelection());

		return layout;
	}

	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Message m = dispatchable_.obtainMessage(MessageId.SHOPPING_CATEGORY_CLICKED, position, 0);
			m.sendToTarget();
		}
	}

	private CategoryListAdapter model_;
	private Handler dispatchable_;
}
