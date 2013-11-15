package com.pushcoin.app.bitsypos;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

public class CheckoutActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );

		// Create the event pump
		EventHub.newInstance( this );

		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );

		// Session manager
		carts_ = CartManager.newInstance( this );

		// Set this activity UI layout
		setContentView(R.layout.checkout_layout);
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "CheckoutActivity" );
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// Remove self from the event hub.
		EventHub.getInstance().unregister( handler_ );
	}

	private Handler handler_;
	private CartManager carts_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<CheckoutActivity> ref_; 

		IncomingHandler(CheckoutActivity ref) {
			ref_ = new WeakReference<CheckoutActivity>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			CheckoutActivity ref = ref_.get();
			if (ref != null)
			{
				switch( msg.what )
				{
				/*
					case MessageId.CART_POOL_CHANGED:
						ref.getSlidingMenu().toggle();
					break;
				*/
				}
			}
		}
	}
}

