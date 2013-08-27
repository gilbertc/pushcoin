package com.pushcoin.icebreaker;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.res.Resources;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RatingBar;
import android.widget.ArrayAdapter;

public class HistoryFragment 
	extends Fragment
	implements ModelEventHandler
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.history_tab, container, false);
		ListView history = (ListView)fragmentRootLayout.findViewById( R.id.transaction_history_list );

		HistoryModelAdapter arrayAdapter = 
			new HistoryModelAdapter(getActivity(), 
				R.layout.history_row, R.id.txn_counterparty_name, 
				R.id.txn_amount, R.id.txn_time, (Controller) getActivity() );
		history.setAdapter(arrayAdapter); 

		return fragmentRootLayout;
	}

	// Invoked by the Controller
	@Override
	public void onAccountHistoryChanged()
	{
		// update this fragment's view
	}

}
