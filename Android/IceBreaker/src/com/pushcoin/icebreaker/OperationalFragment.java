package com.pushcoin.icebreaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import android.content.Context;

public class OperationalFragment extends Fragment
{
	OperationalFragment( Controller ctrl ) {
		ctrl_ = ctrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		View fragmentRootLayout = inflater.inflate(R.layout.operational_fragment, container, false);

		// cache preferences
		prefs_ = getActivity().getPreferences(Context.MODE_PRIVATE);

		// Cache panel buttons, pager
		refreshButton_ = (Button) fragmentRootLayout.findViewById(R.id.refresh_button);
		balanceButton_ = (Button) fragmentRootLayout.findViewById(R.id.balance_button);
		historyButton_ = (Button) fragmentRootLayout.findViewById(R.id.history_button);
		pager_ = (ViewPager) fragmentRootLayout.findViewById(R.id.pager);

		// Use an icon instead of the "Refresh" label
		refreshButton_.setTypeface( Typeface.createFromAsset( getActivity().getAssets(), "fonts/modernpics.otf") );

		// Listen to refresh presses and fire requests
		refreshButton_.setOnClickListener(new OnClickListener() {
			public void onClick(View v) 
			{
				Message m = Message.obtain();
				m.what = MessageId.ACCOUNT_HISTORY_REQUEST;
				ctrl_.post(m);
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

		OperationalPager pagerAdapter = new OperationalPager( getFragmentManager(), ctrl_ );

		// Install the adapter
		pager_.setAdapter(pagerAdapter);

		// Highlight currently selected tab, disable its button
		pager_.setOnPageChangeListener( new PageChangeListener() );

		return fragmentRootLayout;
	}

	private static class OperationalPager extends FragmentPagerAdapter 
	{
		public OperationalPager(FragmentManager fm, Controller ctrl)
		{
			super(fm);
			ctrl_ = ctrl;
		}

		@Override
		public Fragment getItem(int i) 
		{
			Fragment fragment = null;
			if (i == 0) {
				fragment = new BalanceFragment(ctrl_);
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

		final Controller ctrl_;
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

	final Controller ctrl_;
	SharedPreferences prefs_;
	ViewPager pager_;
	Button refreshButton_;
	Button balanceButton_;
	Button historyButton_;
}
