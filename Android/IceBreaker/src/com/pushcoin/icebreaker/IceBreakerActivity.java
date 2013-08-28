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

		// Cache panel buttons, pager
		refreshButton_ = (Button) findViewById(R.id.refresh_button);
		balanceButton_ = (Button) findViewById(R.id.balance_button);
		historyButton_ = (Button) findViewById(R.id.history_button);
		pager_ = (ViewPager)findViewById(R.id.pager);

		// Use an icon instead of the "Refresh" label
		refreshButton_.setTypeface( Typeface.createFromAsset(getAssets(), "fonts/modernpics.otf") );

		// Listen to refresh requests
		refreshButton_.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				fetchAccountHistory();
			}
		});

		// User can swipe or click page-button in the panel
		//
    balanceButton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				pager_.setCurrentItem(TAB_ID_BALANCE, true);
			}
		});
    historyButton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				pager_.setCurrentItem(TAB_ID_HISTORY, true);
			}
		});

		// Manages pages that are part of the pager
		pagerAdapter_ = new IceBreakerPagerAdapter( getFragmentManager() );
		// Install the adapter
		pager_.setAdapter(pagerAdapter_);

		// Highlight currently selected tab, disable its button
		pager_.setOnPageChangeListener( new PageChangeListener() );
	}

	@Override
  protected void onResume() 
	{
		super.onResume();
		fetchAccountHistory();
	}

	/**
		Controller interface
	*/
	@Override
	public void fetchAccountHistory()
	{
		// FIXME: hardcoded MAT (from demo account)
		new DownloadHistoryTask(this).execute("5AAE3A9E939A1E483E384D6B42E70C0AB44F6C04");
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

	private static class IceBreakerPagerAdapter extends FragmentPagerAdapter 
	{
		public IceBreakerPagerAdapter(FragmentManager fm) 
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int i) 
		{
			Fragment fragment = null;
			if (i == 0) {
				fragment = new BalanceFragment();
			} 
			else if (i == 1) {
				fragment = new HistoryFragment();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}
	}

	private class PageChangeListener implements ViewPager.OnPageChangeListener
	{
		@Override
		public void onPageSelected(int position) 
		{
			switch (position) 
			{
				case TAB_ID_BALANCE:
					// balance
					balanceButton_.setEnabled(false);
					balanceButton_.setBackgroundResource(R.color.tab_selected);
					// history
					historyButton_.setEnabled(true);
					historyButton_.setBackgroundResource(R.color.tab_unselected);
					break;
				case TAB_ID_HISTORY:
					// balance
					balanceButton_.setEnabled(true);
					balanceButton_.setBackgroundResource(R.color.tab_unselected);
					// history
					historyButton_.setEnabled(false);
					historyButton_.setBackgroundResource(R.color.tab_selected);
					break;
			}
		}

		/** 
			We don't care about these events
		*/
		@Override
		public void onPageScrollStateChanged(int state)
		{ }

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{ }
	}

	static final int TAB_ID_BALANCE = 0;
	static final int TAB_ID_HISTORY = 1;

	IceBreakerPagerAdapter pagerAdapter_;

	ViewPager pager_;
	Button refreshButton_;
	Button balanceButton_;
	Button historyButton_;

	// Model data
	String status_;
}
