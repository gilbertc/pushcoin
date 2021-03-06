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
import android.support.v13.app.FragmentStatePagerAdapter;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import android.content.Context;

public class OperationalFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Activity holding this fragment is our Controller
		ctrl_ = (Controller) getActivity();

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
			public void onClick(View v) {
				accountHistoryRequest();
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

		// Highlights currently viewed tab in the bar, disable its button
		pager_.setOnPageChangeListener( new PageChangeListener() );

		// kick off download
		accountHistoryRequest();

		return fragmentRootLayout;
	}

	private void accountHistoryRequest()
	{
		Message m = Message.obtain();
		m.what = MessageId.ACCOUNT_HISTORY_REQUEST;
		ctrl_.post(m);
	}

	private static class OperationalPager 
		extends FragmentStatePagerAdapter 
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
				fragment = new HistoryFragment(ctrl_);
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

	Controller ctrl_;
	SharedPreferences prefs_;
	ViewPager pager_;
	Button refreshButton_;
	Button balanceButton_;
	Button historyButton_;
}
