/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.app.bitsypos;

import com.pushcoin.lib.integrator.IntentIntegrator;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
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
import android.widget.TextView;
import android.util.Log;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

public class TabMenuFragment extends Fragment 
{
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "TabMenuFragment" );

		// Cart might have changed while we were gone.
		onCartPoolChanged();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// Remove self from the event hub.
		EventHub.getInstance().unregister( handler_ );
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Listview showing categories.
		View layout = inflater.inflate(R.layout.tab_menu, container, false);

		// Find the tab-listview, set adapter and listener
		ListView menu = (ListView)layout.findViewById( R.id.tab_menu_listview );
		adapter_ = new TabMenuAdapter( getActivity() );
		menu.setAdapter(adapter_);
		// Active cart switcher
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					CartManager.Entry entry = CartManager.getInstance().setActive( position );
					((SlidingActivity)getActivity()).getSlidingMenu().showContent();
				}
			});

		// Button: New Tab
		View newTabBtn = layout.findViewById( R.id.tab_menu_new_tab_button );
		newTabBtn.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					// Ask for tab name
					PromptTabNameFragment.showDialog( getFragmentManager() );
				}
			});

		// Button: Settings
		View settingsBtn = layout.findViewById( R.id.tab_menu_settings_button );
		settingsBtn.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					// Launch integrator configuration
					AppDb.getInstance().getIntegrator().settings( getActivity() );
				}
			});

		// Button: Lock
		View exitBtn = layout.findViewById( R.id.tab_menu_exit_button );
		exitBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});

		return layout;
	}

	private void onCartPoolChanged()
	{
		adapter_.notifyDataSetChanged();
	}

	private TabMenuAdapter adapter_;
	private Handler handler_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<TabMenuFragment> ref_; 

		IncomingHandler(TabMenuFragment ref) {
			ref_ = new WeakReference<TabMenuFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			TabMenuFragment ref = ref_.get();
			if (ref != null)
			{
				Log.v(Conf.TAG, "TabMenuFragment|event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
				switch( msg.what )
				{
					case MessageId.CART_POOL_CHANGED:
						ref.onCartPoolChanged();
					break;
				}
			}
		}
	}
}
