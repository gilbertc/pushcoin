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

import com.pushcoin.ifce.connect.data.Customer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ProgressDialog;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.TextView;
import java.util.List;
import java.lang.ref.WeakReference;

public class CustomerSearchFragment extends Fragment 
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
		EventHub.getInstance().register( handler_, "CustomerSearchFragment" );
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
		View layout = inflater.inflate(R.layout.customer_search_layout, container, false);
		// Prevent darn search box from gaining focus and 
		// causing keyboard to popup when activity shows up ;)
		layout.requestFocus();

		// Place where we keep results from queries
		customers_ = new CustomerListAdapter( getActivity() );

		// Hook up search button listener
		final TextView search = (TextView) layout.findViewById( R.id.customer_search_button );
		search.setOnEditorActionListener( new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if ( actionId == EditorInfo.IME_ACTION_SEARCH )
					{
						v.clearFocus();
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
						String query = search.getText().toString();

						// check if anything to do...
						if (!query.isEmpty()) {
							AppDb.getInstance().asyncFindCustomerWithKeyword( getActivity(), query, EventHub.getInstance() );
						}
					}
					return false;
				}
			});

		// Fire an event whenever user chooses customer from the listview
		ListView show_matches = (ListView)layout.findViewById( R.id.customer_search_list );
		show_matches.setAdapter(customers_);

		// install click-event listener
		show_matches.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Customer entry = (Customer) customers_.getItem( position );
					EventHub.post( MessageId.CUSTOMER_CLICKED, entry );
				}
			});

		return layout;
	}

	private void onQueryUsersReply( Message msg )
	{
		List<Customer> customers = (List<Customer>) msg.obj;
		if (customers.size() > 1) {
			customers_.showData( customers );
		} else {
			customers_.showData( Conf.EMPTY_CUSTOMER_LIST );
		}
	}

	private Handler handler_;
	private CustomerListAdapter customers_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<CustomerSearchFragment> ref_; 

		IncomingHandler(CustomerSearchFragment ref) {
			ref_ = new WeakReference<CustomerSearchFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			CustomerSearchFragment ref = ref_.get();
			if (ref != null)
			{
				switch( msg.what )
				{
					case MessageId.QUERY_USERS_REPLY:
						ref.onQueryUsersReply( msg );
					break;
				}
			}
		}
	}
}
