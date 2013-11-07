package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.util.Log;
import java.util.ArrayList;

public class TabMenuFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Listview showing categories.
		View layout = inflater.inflate(R.layout.tab_menu, container, false);

		// Find the tab-listview, set adapter and listener
		ListView menu = (ListView)layout.findViewById( R.id.tab_menu_listview );
		final TabMenuAdapter model = new TabMenuAdapter( getActivity() );
		menu.setAdapter(model);
		// Active cart switcher
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					CartManager.Entry entry = CartManager.getInstance().setActiveEntry( position );
					EventHub.post( MessageId.ACTIVE_TAB_CHANGED );
				}
			});

		// Hook up the listener for the new-tab button
		Button newTabBtn = (Button) layout.findViewById( R.id.tab_menu_new_tab_button );
		newTabBtn.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					// Ask for tab name
					PromptTabNameFragment.showDialog( getFragmentManager() );
				}
			});

		return layout;
	}
}
