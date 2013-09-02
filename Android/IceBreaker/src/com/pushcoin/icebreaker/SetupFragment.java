package com.pushcoin.icebreaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
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

public class SetupFragment 
	extends Fragment 
{
	SetupFragment( Controller ctrl ) {
		ctrl_ = ctrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// When user finishes typing registration code, we want to
		// show progress bar. So we register for this event.
		ctrl_.registerHandler( handler_, MessageId.REGISTER_DEVICE_REQUEST );
		ctrl_.registerHandler( handler_, MessageId.REGISTER_DEVICE_USER_CANCELED );

		// Inflate setup fragment (with the pager inside)
		View fragmentRootLayout = inflater.inflate(R.layout.setup_fragment, container, false);

		// Locate the pager within the layout
		pager_ = (NoSwipingViewPager) fragmentRootLayout.findViewById(R.id.pager);
		// Turn off swiping; buttons direct which page is shown
		pager_.setSwipingEnabled(false);

		// Install the adapter with the pager
		pager_.setAdapter( new SetupPager( getFragmentManager(), ctrl_ ) );

		return fragmentRootLayout;
	}

	private static class SetupPager 
		extends FragmentStatePagerAdapter 
	{
		public SetupPager(FragmentManager fm, Controller ctrl)
		{
			super(fm);
			ctrl_ = ctrl;
		}

		@Override
		public Fragment getItem(int i) 
		{
			Fragment fragment = null;
			if (i == TAB_ID_ENTER_REGISTRATION_CODE) {
				fragment = new EnterRegistrationCodeFragment( ctrl_ );
			} 
			else if (i == TAB_ID_SUBMIT_REGISTRATION) {
				fragment = new SubmitRegistrationCodeFragment( ctrl_ );
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		final Controller ctrl_;
	}

	/** Dispatch events. */
	private Handler handler_ = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch( msg.what )
			{
				case MessageId.REGISTER_DEVICE_REQUEST:
					pager_.setCurrentItem(TAB_ID_SUBMIT_REGISTRATION, true);
				break;
				case MessageId.REGISTER_DEVICE_USER_CANCELED:
					pager_.setCurrentItem(TAB_ID_ENTER_REGISTRATION_CODE, true);
				break;
			}
		}
	};

	static final int TAB_ID_ENTER_REGISTRATION_CODE = 0;
	static final int TAB_ID_SUBMIT_REGISTRATION = 1;

	final Controller ctrl_;
	NoSwipingViewPager pager_;
}
