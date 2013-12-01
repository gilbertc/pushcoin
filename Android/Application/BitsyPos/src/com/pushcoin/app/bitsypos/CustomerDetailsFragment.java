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

package com.pushcoin.app.bitsypos;

import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.Amount;
import com.pushcoin.ifce.connect.listeners.ChargeResultListener;
import com.pushcoin.lib.integrator.IntentIntegrator;

import com.pushcoin.ifce.connect.data.Customer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.lang.ref.WeakReference;
import android.util.Log;

public class CustomerDetailsFragment extends Fragment 
{
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "CustomerDetailsFragment" );
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// Remove self from the event hub.
		EventHub.getInstance().unregister( handler_ );
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// User-details layout
		rootView_ = inflater.inflate(R.layout.customer_details_layout, container, false);
		// Cached widgets
		firstName_ = (TextView) rootView_.findViewById(R.id.customer_details_fname);
		lastName_ = (TextView) rootView_.findViewById(R.id.customer_details_lname);
		title_ = (TextView) rootView_.findViewById(R.id.customer_details_title);
		identifier_ = (TextView) rootView_.findViewById(R.id.customer_details_customer_identifier);
		balanceLabel_ = (TextView) rootView_.findViewById(R.id.customer_details_customer_balance_label);
		balance_ = (TextView) rootView_.findViewById(R.id.customer_details_customer_balance);
		mugshot_ = (ImageView) rootView_.findViewById(R.id.customer_details_mugshot);
		chargeBtn_ = (Button) rootView_.findViewById(R.id.customer_details_charge_button);

		// cache few color resources
		btnTextColorOn_ = getResources().getColor( android.R.color.white );
		btnTextColorOff_ = getResources().getColor( R.color.lightui_lightgray );

		// handle charge click
		chargeBtn_.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// we shouldn't even be here if there is no user data...
					if (user_ == null) return;

					// Issue the idle request to turn off readers while we are
					// submitting the manual charge...
					IntentIntegrator integrator = AppDb.getInstance().getIntegrator();
					integrator.idle();

					Cart cart = CartManager.getInstance().getActiveCart();
					Transaction charge = cart.createChargeTransaction( user_ );

					// Do we owe anything at this point?
					if (charge != null)
					{
						ChargeParams params = new ChargeParams();
						BigDecimal chargeAmount = charge.getAmount();
						params.setAccountId( user_.accountId );
						params.setClientRequestId( charge.getClientTransactionId() );
						params.setPayment(
							new Amount(chargeAmount.unscaledValue().longValue(), -chargeAmount.scale()));
						integrator.charge(params, (ChargeResultListener) getActivity() );
					} 
				}
			});

		return rootView_;
	}
	
	private void onCustomerDetailsAvailable( Customer user )
	{
		// Store user presently displayed (or null)
		user_ = user;

		if (user != null)
		{
			firstName_.setText( user.firstName );
			lastName_.setText( user.lastName );
			title_.setText( user.title );
			identifier_.setText( user.identifier );
			if (user.balance != null)
			{
				balanceLabel_.setVisibility(View.VISIBLE);
				balanceLabel_.setText( "Balance as of " + Util.prettyRelativeTime( System.currentTimeMillis(), user.balanceAsOf));

				balance_.setText( NumberFormat.getCurrencyInstance().format( user.balance.asDecimal()));
				balance_.setVisibility(View.VISIBLE);
			} 
			else
			{
				balanceLabel_.setVisibility(View.INVISIBLE);
				balance_.setVisibility(View.INVISIBLE);
			}
			if (user.mugshot != null) {
				mugshot_.setImageBitmap( user.mugshot );
			}
			rootView_.setVisibility(View.VISIBLE);

			// Turn on/off charge-button depending if cart is paid off
			onTransactionStatusChanged( CartManager.getInstance().getActive() );

		} else {
			rootView_.setVisibility(View.GONE);
		}
	}
	
	private void onQueryUsersReply( Message msg )
	{
		List<Customer> customers = (List<Customer>) msg.obj;
		if (customers.size() == 1) {
			onCustomerDetailsAvailable( customers.get(0) ); 
		} else {
			onCustomerDetailsAvailable( null ); 
		}
	}

	private void onTransactionStatusChanged( CartManager.Entry cartHolder )
	{
		// Turn on/off buttons depending on cart state.
		if ( cartHolder.cart.isPaid() ) {
			Util.disableButton( chargeBtn_, R.drawable.btn_gray, btnTextColorOff_);
		} else {
			Util.enableButton( chargeBtn_, R.drawable.btn_blue, btnTextColorOn_);
		}
	}

	private Handler handler_;
	private Customer user_;
	// UI with user info
	private View rootView_;
	private TextView firstName_;
	private TextView lastName_;
	private TextView title_;
	private TextView identifier_;
	private TextView balanceLabel_;
	private TextView balance_;
	private ImageView mugshot_;
	private Button chargeBtn_;

	private int btnTextColorOn_;
	private int btnTextColorOff_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<CustomerDetailsFragment> ref_; 

		IncomingHandler(CustomerDetailsFragment ref) {
			ref_ = new WeakReference<CustomerDetailsFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			CustomerDetailsFragment ref = ref_.get();
			if (ref != null)
			{
				switch( msg.what )
				{
					case MessageId.CUSTOMER_CLICKED:
						ref.onCustomerDetailsAvailable( (Customer) msg.obj );
					break;

					case MessageId.QUERY_USERS_REPLY:
						ref.onQueryUsersReply( msg );
					break;

					case MessageId.TRANSACTION_STATUS_CHANGED:
						ref.onTransactionStatusChanged( CartManager.getInstance().getActive() );
					break;
				}
			}
		}
	}
}
