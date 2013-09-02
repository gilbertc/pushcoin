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
{
	public HistoryFragment(Controller ctrl)
	{
		ctrl_ = ctrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		ctrl_.registerHandler( handler_, MessageId.MODEL_CHANGED );

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.history_tab, container, false);
		ListView history = (ListView)fragmentRootLayout.findViewById( R.id.transaction_history_list );

		arrayAdapter_ = new HistoryModelAdapter( getActivity(), ctrl_ );
		history.setAdapter(arrayAdapter_); 

		return fragmentRootLayout;
	}

	/** Dispatch events. */
	private Handler handler_ = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch( msg.what )
			{
				case MessageId.MODEL_CHANGED:
					arrayAdapter_.notifyDataSetChanged();
				break;
			}
		}
	};

	final Controller ctrl_;

	HistoryModelAdapter arrayAdapter_;
}
