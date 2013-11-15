package com.pushcoin.app.bitsypos;

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
import java.util.ArrayList;

public class CustomerSearchFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Listview showing categories.
		View layout = inflater.inflate(R.layout.customer_search_layout, container, false);
		// Prevent darn search box from gaining focus and 
		// causing keyboard to popup 
		layout.requestFocus();

		/*
		ListView menu = (ListView)layout.findViewById( R.id.category_menu );

		final CategoryListAdapter model = new CategoryListAdapter(getActivity(), R.layout.category_menu_row, R.id.category_menu_label);
		menu.setAdapter(model);

		// install click-event listener
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					CategoryListAdapter.Entry entry = model.getEntry( position );
					EventHub.post( MessageId.CATEGORY_CLICKED, entry.tag_id );
				}
			});
		*/

		return layout;
	}
}
