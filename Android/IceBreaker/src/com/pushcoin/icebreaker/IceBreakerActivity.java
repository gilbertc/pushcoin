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

		// link refresh button with fetching remote data
		refreshButton_ = (Button) findViewById(R.id.refresh_button);
		refreshButton_.setTypeface( Typeface.createFromAsset(getAssets(), "fonts/modernpics.otf") );
		refreshButton_.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				fetchAccountHistory();
			}
		});

		pagerAdapter_ = new IceBreakerPagerAdapter( getFragmentManager() );

		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter_);
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
	public String getStatus()
	{
		return status_;
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
			Fragment fragment = new BalanceFragment();
			return fragment;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Balance";
		}
	}

	IceBreakerPagerAdapter pagerAdapter_;

	Button refreshButton_;

	// Model data
	String status_;
}
