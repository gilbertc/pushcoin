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
	implements EnterRegistrationCodeFragment.OnSubmitRegistrationCode
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
		pager_ = (ViewPager) fragmentRootLayout.findViewById(R.id.pager);

		// Install the adapter with the pager
		pager_.setAdapter( new SetupPager( getFragmentManager(), this ) );

		return fragmentRootLayout;
	}

	@Override
	public void onSubmitRegistrationCode( String code )
	{
	}

	private static class SetupPager 
		extends FragmentPagerAdapter 
	{
		public SetupPager(FragmentManager fm, EnterRegistrationCodeFragment.OnSubmitRegistrationCode handler )
		{
			super(fm);

			enterRegistrationCode_ = new EnterRegistrationCodeFragment( handler );
			// submitRegistration_ = new SubmitRegistrationFragment( handler );
		}

		@Override
		public Fragment getItem(int i) 
		{
			Fragment fragment = null;
			if (i == 0) {
				fragment = enterRegistrationCode_;
			} 
			else if (i == 1) {
				//fragment = submitRegistration_;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 1;
		}

		EnterRegistrationCodeFragment enterRegistrationCode_;
		//SubmitRegistrationFragment submitRegistration_;
	}

	final Controller ctrl_;
	ViewPager pager_;
}
