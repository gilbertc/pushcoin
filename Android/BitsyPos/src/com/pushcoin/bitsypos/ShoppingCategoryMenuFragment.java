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
import java.util.ArrayList;

public class ShoppingCategoryMenuFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Listview showing categories.
		View layout = inflater.inflate(R.layout.shopping_category_menu, container, false);
		ListView menu = (ListView)layout.findViewById( R.id.shopping_category_menu );

		final CategoryListAdapter model = new CategoryListAdapter(getActivity(), R.layout.shopping_category_menu_row, R.id.shopping_category_menu_label);
		menu.setAdapter(model);

		// install click-event listener
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					CategoryListAdapter.Entry entry = model.getEntry( position );
					EventHub.post( MessageId.SHOPPING_CATEGORY_CLICKED, entry.tag_id );
				}
			});

		return layout;
	}
}
