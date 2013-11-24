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
import android.os.Handler;
import android.os.Message;
import android.os.Build;
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
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import com.pushcoin.Binascii;

public class IceBreakerActivity 
	extends Activity
	implements Controller
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i( Conf.TAG, "build-info|make="+Build.MANUFACTURER+";model"+Build.MODEL+";product="+Build.PRODUCT );
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Register self as a handler of certain types of messages
		registerHandler( handler_, MessageId.ACCOUNT_HISTORY_REQUEST );
		registerHandler( handler_, MessageId.REGISTER_DEVICE_REQUEST );
		registerHandler( handler_, MessageId.REGISTER_DEVICE_SUCCESS );
		registerHandler( handler_, MessageId.ACCOUNT_HISTORY_REPLY );
		registerHandler( handler_, MessageId.DEVICE_NOT_ACTIVE );
		// Messages emitted by this Controller - DO NOT SUBSCRIBE
		// * MODEL_CHANGED
	}

	@Override
  protected void onResume() 
	{
		super.onResume();
		// The app can be in one of two modes:
		//  * configuration mode (fresh install, no MAT yet)
		//  * operating mode
		//
		pickMode();
	}

	/**
		Controller interface
	*/
	@Override
	public String getPageTitle()
	{
		if (txnHistory_ != null ) {
			return "Balance";
		} else {
			return "Not available";
		}
	}

	@Override
	public String getBalance()
	{
		if (txnHistory_ != null ) {
			return PcosHelper.prettyAmount( txnHistory_.balance, Conf.DEFAULT_CURRENCY );
		} else {
			return "";
		}
	}

	@Override
	public String getBalanceTime()
	{
		if (txnHistory_ != null ) {
			return PcosHelper.prettyTime(this, txnHistory_.balanceTimeEpoch);
		} else {
			return "";
		}
	}

	@Override
	public String getStatus()
	{
		return status_;
	}

	@Override
	public PcosHelper.TransactionInfo getTransaction(int index)
	{
		if (txnHistory_ != null && index < txnHistory_.transactionInfo.length ) {
			return txnHistory_.transactionInfo[index];
		} else {
			throw new RuntimeException("No transaction record at index "+index);
		}
	}

	@Override
	public PcosHelper.TransactionInfo getRecentTransaction()
	{
		if (txnHistory_ != null && txnHistory_.transactionInfo.length > 0) {
			return txnHistory_.transactionInfo[0];
		}
		return null;
	}

	@Override
	public void castRating( String txnId, int rating )
	{
		new CastRatingTask(this, mat_, txnId, rating).execute();
	}

	@Override
	public void registerHandler( Handler h, int messageId )
	{
		List<Handler> slot = messageReceiver_.get(messageId);
		if (slot == null)
		{
			slot = new ArrayList<Handler>();
			messageReceiver_.put(messageId, slot);		
		}
		slot.add(h);
	}

	@Override
	public void post( Message m )
	{
		List<Handler> slot = messageReceiver_.get( m.what );
		if (slot != null)
		{
			for (Handler h: slot) {
				Message nm = Message.obtain( m );
				nm.setTarget( h );
				nm.sendToTarget();
			}
		}
	}

	@Override
	public int getHistorySize()
	{
		if (txnHistory_ != null) {
			return txnHistory_.transactionInfo.length;
		} else {
		  return 0;
		}
	}

	@Override
	public void reload()
	{
		long nowEpoch = PcosHelper.getEpochUtc();
		// Throttle number of refreshes.
		if (requestTimestamps_.size() < Conf.THROTTLE_MAX_REQUESTS_PER_WINDOW)
		{
			requestTimestamps_.add( new Long(nowEpoch) );
			new DownloadHistoryTask(this, mat_, txnHistory_).execute();
			enqueuedReload_ = 0;
		} 
		else
		{
			// check oldest request timestamp
			Long reqTm = requestTimestamps_.get(0);
			long diffTm = nowEpoch - reqTm;
			if ( diffTm > Conf.THROTTLE_REQUEST_WINDOW_DURATION )
			{
				// pop oldest request
				requestTimestamps_.remove(0);
				// add this request
				requestTimestamps_.add( new Long(nowEpoch) );
				new DownloadHistoryTask(this, mat_, txnHistory_).execute();
				enqueuedReload_ = 0;
			}
			else 
			{
				long waitUntil = Conf.THROTTLE_REQUEST_WINDOW_DURATION-diffTm+1;
				Log.v( Conf.TAG, "throttled-until-seconds="+waitUntil );

				// Schedule reload in the future, when throttle window expires
				if (nowEpoch - enqueuedReload_ > Conf.THROTTLE_REQUEST_WINDOW_DURATION)
				{
					enqueuedReload_ = nowEpoch;
					Message deferredReload = handler_.obtainMessage(MessageId.ACCOUNT_HISTORY_REQUEST);
					handler_.sendMessageDelayed(deferredReload, waitUntil*1000);
				} else {
					Log.v( Conf.TAG, "reload-ignored;already-scheduled" );
				}
			}
		}
	}

	private void pickMode()
	{
		String encodedMat = getPreferences(Context.MODE_PRIVATE).getString(Conf.PREFS_KEY_MAT_KEY, null);
		if (encodedMat != null) {
			mat_ = Binascii.unhexlify( encodedMat );
		} else {
			mat_ = null;
		}

		// This will hold the fragment appropriate to the mode we are in.
		Fragment modeViewHandler;

		// if has MAT, we must have been configured
		if (mat_ == null) {
			// We are in configuration mode
			modeViewHandler = new SetupFragment();
		}
		else { 
			// Instantatiate operational fragment
			modeViewHandler = new OperationalFragment();
		}

		// We picked the fragment, pass any parameters to it
		modeViewHandler.setArguments(getIntent().getExtras());

		// Show the fragment
		getFragmentManager().beginTransaction()
			.replace( R.id.main_fragment, modeViewHandler )
			.commit();
	}

	private long recentRequestTime()
	{
		if (requestTimestamps_.isEmpty()) {
			return 0;
		} else {
			return requestTimestamps_.get(requestTimestamps_.size() - 1);
		}
	}

	/**
		On error, tasks update the status (on UI thread).
	*/
	public void setStatus(String v)
	{
		status_ = v;
		Message m = Message.obtain();
		m.what = MessageId.STATUS_CHANGED;
		post(m);
	}

	/** Dispatch events. */
	private Handler handler_ = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch( msg.what )
			{
				case MessageId.ACCOUNT_HISTORY_REQUEST:
					reload();
				break;
				case MessageId.REGISTER_DEVICE_REQUEST:
					new RegisterDeviceTask(IceBreakerActivity.this).execute( (String) msg.obj );
				break;
				case MessageId.REGISTER_DEVICE_SUCCESS:
					onDeviceRegistered( (PcosHelper.RegisterAckResult) msg.obj );
				break;
				case MessageId.ACCOUNT_HISTORY_REPLY:
					onTxnHistoryReply( (PcosHelper.TxnHistoryReply) msg.obj );
				break;
				case MessageId.DEVICE_NOT_ACTIVE:
					onDeviceNotRegistered();
				break;
			}
		}
	};

	private void onDeviceRegistered(PcosHelper.RegisterAckResult ack)
	{
		SharedPreferences.Editor store = getPreferences(Context.MODE_PRIVATE).edit();
		store.putString(Conf.PREFS_KEY_MAT_KEY, Binascii.hexlify( ack.mat ) );
		store.commit();

		// We are leaving configuration mode...
		restart();
	}

	private void onDeviceNotRegistered()
	{
		SharedPreferences.Editor store = getPreferences(Context.MODE_PRIVATE).edit();
		store.clear().commit();

		// We are entering configuration mode...
		restart();
	}


	private void onTxnHistoryReply(PcosHelper.TxnHistoryReply txData)
	{
		txnHistory_ = txData;
		status_ = "";

		// Notify of changes applied
		Message m = Message.obtain();
		m.what = MessageId.MODEL_CHANGED;
		post(m);
	}

	/** 
		Restarts this activity.
	*/
	private void restart() 
	{
    Intent intent = getIntent();
    overridePendingTransition(0, 0);
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    finish();

    overridePendingTransition(0, 0);
    startActivity(intent);
	}

	static final int TAB_ID_BALANCE = 0;
	static final int TAB_ID_HISTORY = 1;

	private final Object lock_ = new Object();
	SharedPreferences prefs_;
	ViewPager pager_;
	byte[] mat_;
	ArrayList<Long> requestTimestamps_ = new ArrayList<Long>();
	long enqueuedReload_ = 0;

	// Model data
	String status_ = "";
	PcosHelper.TxnHistoryReply txnHistory_;

	TreeMap<Integer, List<Handler> > messageReceiver_ =
		new TreeMap<Integer, List<Handler> >();
}
