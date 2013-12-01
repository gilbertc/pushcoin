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
import java.lang.ref.WeakReference;

public class TransactionMonitorFragment extends Fragment 
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
		EventHub.getInstance().register( handler_, "TransactionMonitorFragment" );
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
		View layout = inflater.inflate(R.layout.transaction_monitor_layout, container, false);

		ListView transactions = (ListView)layout.findViewById( R.id.transaction_list );

		model_ = new TransactionListAdapter(getActivity());
		transactions.setAdapter(model_);

		// install click-event listener
		/*
		transactions.setOnItemClickListener(new AdapterView.OnItemClickListener()
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

	private void onTransactionStatusChanged()
	{
		model_.reloadData();
	}

	private Handler handler_;
	private TransactionListAdapter model_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<TransactionMonitorFragment> ref_; 

		IncomingHandler(TransactionMonitorFragment ref) {
			ref_ = new WeakReference<TransactionMonitorFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			TransactionMonitorFragment ref = ref_.get();
			if (ref != null)
			{
				switch( msg.what )
				{
					case MessageId.TRANSACTION_STATUS_CHANGED:
						ref.onTransactionStatusChanged();
					break;
				}
			}
		}
	}
}
