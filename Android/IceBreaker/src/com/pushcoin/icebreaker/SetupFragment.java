package com.pushcoin.icebreaker;

import android.app.Activity;
import android.os.Bundle;
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

public class SetupFragment 
	extends Fragment 
	implements 
		EnterRegistrationCodeFragment.OnSubmitRegistrationCode,
		SubmitRegistrationCodeFragment.OnRegistrationProgress
{
	SetupFragment( Controller ctrl ) {
		ctrl_ = ctrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Inflate setup fragment (with the pager inside)
		View fragmentRootLayout = inflater.inflate(R.layout.setup_fragment, container, false);

		// Locate the pager within the layout
		pager_ = (NoSwipingViewPager) fragmentRootLayout.findViewById(R.id.pager);
		// Turn off swiping; buttons direct which page is shown
		pager_.setSwipingEnabled(false);

		// Install the adapter with the pager
		pager_.setAdapter( new SetupPager( getFragmentManager(), this, this ) );

		return fragmentRootLayout;
	}

	@Override
	public void onSubmitRegistrationCode( String code )
	{
		pager_.setCurrentItem(TAB_ID_SUBMIT_REGISTRATION, true);
	}

	@Override
	public void onUserCanceled()
	{
		pager_.setCurrentItem(TAB_ID_ENTER_REGISTRATION_CODE, true);
	}

	private static class SetupPager 
		extends FragmentPagerAdapter 
	{
		public SetupPager(FragmentManager fm, 
			EnterRegistrationCodeFragment.OnSubmitRegistrationCode handler1,
			SubmitRegistrationCodeFragment.OnRegistrationProgress handler2)
		{
			super(fm);

			enterRegistrationCode_ = new EnterRegistrationCodeFragment( handler1 );
			submitRegistration_ = new SubmitRegistrationCodeFragment( handler2 );
		}

		@Override
		public Fragment getItem(int i) 
		{
			Fragment fragment = null;
			if (i == TAB_ID_ENTER_REGISTRATION_CODE) {
				fragment = enterRegistrationCode_;
			} 
			else if (i == TAB_ID_SUBMIT_REGISTRATION) {
				fragment = submitRegistration_;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		EnterRegistrationCodeFragment enterRegistrationCode_;
		SubmitRegistrationCodeFragment submitRegistration_;
	}

	static final int TAB_ID_ENTER_REGISTRATION_CODE = 0;
	static final int TAB_ID_SUBMIT_REGISTRATION = 1;

	final Controller ctrl_;
	NoSwipingViewPager pager_;
}
