package com.pushcoin.app.bitsypos;

import com.pushcoin.lib.integrator.IntentIntegrator;
import com.pushcoin.ifce.connect.data.Amount;
import com.pushcoin.ifce.connect.data.ChargeResult;
import com.pushcoin.ifce.connect.data.Customer;
import com.pushcoin.ifce.connect.data.Error;
import com.pushcoin.ifce.connect.data.PollParams;
import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.ifce.connect.listeners.PollResultListener;

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
import android.widget.Toast;
import android.content.Context;
import android.util.Log;
import java.util.List;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;

public class CheckoutActivity 
	extends Activity
	implements PollResultListener
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

		// Set this activity UI layout
		setContentView(R.layout.checkout_layout);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		// Let PushCoin service know we are back
		Log.v( Conf.TAG, "bootstrapping integrator" );
		AppDb.getInstance().getIntegrator().bootstrap();
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "CheckoutActivity" );
		// ready to accept payments
		waitForPayment();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		// Stop accepting payments
		AppDb.getInstance().getIntegrator().idle();

		// Remove self from the event hub.
		EventHub.getInstance().unregister( handler_ );
	}

	private Handler handler_;

	private void waitForPayment()
	{
		IntentIntegrator integrator = 
			AppDb.getInstance().getIntegrator();

		Cart cart = CartManager.getInstance().getActiveCart();
		Transaction charge = cart.createChargeTransaction();

		// Do we owe anything at this point?
		if (charge != null)
		{
			PollParams params = new PollParams();
			BigDecimal chargeAmount = charge.getAmount();
			params.setPayment(
				new Amount(chargeAmount.unscaledValue().longValue(), -chargeAmount.scale()));
			integrator.poll(params, this);
			Toast.makeText(this, "Waiting for $"+chargeAmount, Toast.LENGTH_LONG).show();
		} 
		else {
			integrator.idle();
		}
	}

	/**
		Polling on device resulted in a user-query (ie thumb-scan)
	*/
  @Override
  public void onResult(QueryResult result)
	{
		List<Customer> customers = result.getCustomers();
		Message msg = Message.obtain(null, MessageId.QUERY_USERS_REPLY, customers);
		handler_.handleMessage(msg);
  }
  
	/**
		Successful charge transaction.
	*/
  @Override
  public void onResult(ChargeResult result)
	{
		Cart cart = CartManager.getInstance().getActiveCart();
		// FIXME: look up transaction by client-transaction-id
		Transaction transaction = cart.getTransaction(0);
		cart.updateTransaction( transaction.approved(result.getTrxId(), result.getUtc()) );
  }

	/**
		Error polling on device.
	*/
  @Override
  public void onResult(Error err)
	{
		Toast.makeText(this, "Error: " + err.getReason(), Toast.LENGTH_LONG)
			.show();
  }

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
					case MessageId.CART_CONTENT_CHANGED:
						ref.waitForPayment();
					break;
				}
			}
		}
	}
}

