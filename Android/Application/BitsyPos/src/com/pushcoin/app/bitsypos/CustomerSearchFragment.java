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
import java.util.List;

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

		ListView show_matches = (ListView)layout.findViewById( R.id.customer_list );

		final CustomerListAdapter model = new CustomerListAdapter( getActivity() );
		show_matches.setAdapter(model);

		// TODO: REMOVE BELOW & PLUG async-Queue-Handler 
		// since data arrives on background thread
		AppDb.getInstance().asyncFindCustomerWithKeyword( "", new AppDb.FindCustomerWithKeywordReply() {
				@Override
				public void onCustomerRecordFound(List<Customer> data) {
					model.showData( data );
				}
			});

		// install click-event listener
		show_matches.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Customer entry = (Customer) model.getItem( position );
					EventHub.post( MessageId.CUSTOMER_DETAILS_AVAILABLE, entry );
				}
			});

		return layout;
	}
}
