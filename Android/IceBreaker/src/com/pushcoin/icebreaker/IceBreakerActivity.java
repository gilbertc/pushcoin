package com.pushcoin.icebreaker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import android.content.Context;

public class IceBreakerActivity 
	extends Activity
	implements Controller
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// The app can be in one of two modes:
		//  * configuration mode (fresh install, no MAT yet)
		//  * operating mode
		//
		mat_ = getPreferences(Context.MODE_PRIVATE).getString(PREF_KEY_MAT_KEY, null);

		// This will hold the fragment approprate to the mode we are in.
		Fragment modeViewHandler;

		// if has MAT, we must have been configured
		if (mat_ == null) {
			// We are in configuration mode
			modeViewHandler = new SetupFragment( this );
		}
		else { 
			// Instantatiate operational fragment
			modeViewHandler = new OperationalFragment( this );
		}

		// We picked the fragment, pass any parameters to it
		modeViewHandler.setArguments(getIntent().getExtras());

		// Show the fragment
		getFragmentManager().beginTransaction()
			.add( R.id.main_fragment, modeViewHandler )
			.commit();
	}

	@Override
  protected void onResume() 
	{
		super.onResume();
	}

	/**
		Controller interface
	*/
	@Override
	public void fetchAccountHistory()
	{
		new DownloadHistoryTask(this).execute(mat_);
	}

	@Override
	public String getBalance()
	{
		return "$0.00";
	}

	@Override
	public String getStatus()
	{
		return status_;
	}

	@Override
	public TransactionRecord getTransaction(int index)
	{
		TransactionRecord	txn = new TransactionRecord();
		if ( index % 2 == 0 ) {
			txn.counterparty = "Wheaton";
			txn.amount = "$51.33";
			txn.utctime = "12:32 PM";
		}
		else if ( index % 3 == 0 ) {
			txn.counterparty = "Wheaton Academy Lunch";
			txn.amount = "$511.33";
			txn.utctime = "Aug 12 '04";
			txn.utctime = "12:32 PM";
		}
		else if ( index % 4 == 0 ) {
			txn.counterparty = "Wheaton Academy";
			txn.amount = "$5.33";
			txn.utctime = "9:01 PM";
		}
		else {
			txn.counterparty = "Wheaton Academy Lunch";
			txn.amount = "$10.33";
			txn.utctime = "1:11 PM";
		}
		return txn;
	}

	@Override
	public int getHistorySize()
	{
		return 20;
	}

	/**
		These setters are called by the Downloader Helper
		after fetching remote data. 

		For now, synchronization is not required as all setters are
		called from the UI thread.
	*/
	public void setStatus(String v) {
		status_ = v;
	}

	static final int TAB_ID_BALANCE = 0;
	static final int TAB_ID_HISTORY = 1;
	static final String PREF_KEY_MAT_KEY = "mat";

	SharedPreferences prefs_;
	ViewPager pager_;
	String mat_;
	Button refreshButton_;
	Button balanceButton_;
	Button historyButton_;

	// Model data
	String status_;
}
