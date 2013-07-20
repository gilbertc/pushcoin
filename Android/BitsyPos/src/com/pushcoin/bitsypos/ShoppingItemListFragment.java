package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.ArrayList;

public class ShoppingItemListFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Store activity's dispacher
		dispatchable_ = ((IDispatcher)getActivity()).getDispachable();

		// Inflate the layout for this fragment
		GridView view = (GridView)inflater.inflate(R.layout.shopping_item_list, container, false);

		// install click-event listener
		view.setOnItemClickListener(new ListSelection());

		return view;
	}

	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Message m = dispatchable_.obtainMessage(MessageId.SHOPPING_ITEM_CLICKED, position, 0);
			m.sendToTarget();
		}
	}

	private Handler dispatchable_;
}
