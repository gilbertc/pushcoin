package com.pushcoin.app.bitsypos;

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

		// Hook up the listener for the new-tab button
		TextView newTabBtn = (TextView) layout.findViewById( R.id.tab_menu_new_tab_button );
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
