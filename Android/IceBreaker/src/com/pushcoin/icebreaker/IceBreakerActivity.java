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
import com.pushcoin.Binascii;

public class IceBreakerActivity 
	extends Activity
	implements Controller
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i( TAG, "build-info|make="+Build.MANUFACTURER+";model"+Build.MODEL+";product="+Build.PRODUCT );
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Register self as a handler of certain types of messages
		registerHandler( handler_, MessageId.FETCH_ACCOUNT_HISTORY );
		registerHandler( handler_, MessageId.REGISTER_DEVICE_REQUEST );
		registerHandler( handler_, MessageId.REGISTER_DEVICE_SUCCESS );
		// Messages emitted by this Controller - DO NOT SUBSCRIBE
		// * MODEL_CHANGED

		// The app can be in one of two modes:
		//  * configuration mode (fresh install, no MAT yet)
		//  * operating mode
		//
		String encodedMat = getPreferences(Context.MODE_PRIVATE).getString(Conf.PREFS_KEY_MAT_KEY, null);
		if (encodedMat != null) {
			mat_ = Binascii.unhexlify( encodedMat );
		}

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
		return 20;
	}

	/**
		Before updating model data, callers must wrap
		updates between begin/end calls.

		"Bad call sequence" exception will be thrown otherwise.
	*/
	public void beginModelUpdates() {
	}

	public void endModelUpdates() 
	{
		// Notify of changes applied
		Message m = Message.obtain();
		m.what = MessageId.MODEL_CHANGED;
		post(m);
	}

	/**
		These setters are called by "tasks".

		Synchronization is not required as all setters are
		called from the UI thread.
	*/
	public void setStatus(String v) {
		status_ = v;
	}

	/** Dispatch events. */
	private Handler handler_ = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch( msg.what )
			{
				case MessageId.FETCH_ACCOUNT_HISTORY:
					new DownloadHistoryTask(IceBreakerActivity.this, mat_).execute();
				break;
				case MessageId.REGISTER_DEVICE_REQUEST:
					new RegisterDeviceTask(IceBreakerActivity.this).execute( (String) msg.obj );
				break;
				case MessageId.REGISTER_DEVICE_SUCCESS:
					onDeviceRegistered( (PcosHelper.RegisterAckResult) msg.obj );
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
	static final String TAG = "IceBr-Ctrl";

	private final Object lock_ = new Object();
	SharedPreferences prefs_;
	ViewPager pager_;
	byte[] mat_;
	Button refreshButton_;
	Button balanceButton_;
	Button historyButton_;

	// Model data
	String status_;

	TreeMap<Integer, List<Handler> > messageReceiver_ =
		new TreeMap<Integer, List<Handler> >();
}
