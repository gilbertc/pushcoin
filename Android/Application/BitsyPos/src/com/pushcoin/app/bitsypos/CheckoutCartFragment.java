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

import de.timroes.swipetodismiss.SwipeDismissList;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.app.Fragment;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.util.Log;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.lang.ref.WeakReference;

public class CheckoutCartFragment extends Fragment
{
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );

		// cache few color resources
		btnTextColorOn_ = getResources().getColor( android.R.color.white );
		btnTextColorOff_ = getResources().getColor( R.color.lightui_lightgray );
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "CheckoutCartFragment" );

		// Restore cart contents
		onCartContentChanged( CartManager.getInstance().getActive() );
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
		Context ctx = getActivity();

		// Cart adapter
		adapter_ = new CartEntryArrayAdapter(ctx,
			R.layout.checkout_cart_row,
			R.id.checkout_cart_entry_title,
			R.id.checkout_cart_entry_price,
			R.id.checkout_cart_entry_note);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.checkout_cart, container, false);

		// Cache cart widgets
		cartTotal_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_total);
		tabName_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_tab_name);
		chargeAmountLabel_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_charge_amount_label);
		chargeAmount_ = (EditText) cartLayout.findViewById(R.id.checkout_cart_charge_amount);
		amountDueLabel_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_due_amount_label);
		amountDue_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_due_amount);
		discountLabel_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_dicount_label);
		discountValue_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_discount);
		discountPct_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_dicount_pct);
		cartPaidOffLabel_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_paid_off_label);
		btnFinished_ = (Button) cartLayout.findViewById(R.id.checkout_cart_finished_button);

		// Handle finished-click
		btnFinished_.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					CartManager carts = CartManager.getInstance();
					CartManager.Entry cartEntry = carts.getActive();
					if ( !cartEntry.cart.isPaid() )
					{
						// Confirm user really wants to dismiss upaid cart
						DismissNonEmptyCartDialog.showDialog( getFragmentManager() );
					}
					else
					{
						carts.remove( cartEntry );
						getActivity().finish();
					}
				}
			});

		// Handle charge amount
		chargeAmount_.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if ( actionId == EditorInfo.IME_ACTION_DONE ) {
						v.clearFocus();
					}
					return false;
				}
			});

		chargeAmount_.setOnFocusChangeListener( new View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					EditText field = (EditText) v;
					if (!hasFocus)
					{
						Cart cart = CartManager.getInstance().getActiveCart();
						// Update only if changed
						String newValStr = field.getText().toString();
						BigDecimal oldVal = cart.getChargeAmount();
						if ( !newValStr.isEmpty() && !newValStr.equals(oldVal.toString()) ) 
						{
							try {
								cart.setChargeAmount( new BigDecimal( newValStr ) );
							} catch (RuntimeException e) { }
						} 
						field.setText( NumberFormat.getCurrencyInstance().format(oldVal) );
						hideKeyboard();
					} 
					else {
						clearFieldShowKeyboard( field );
					}
				}
			});

		// Handle applied discount value
		discountValue_.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if ( actionId == EditorInfo.IME_ACTION_DONE ) {
						v.clearFocus();
					}
					return false;
				}
			});

		discountValue_.setOnFocusChangeListener( new View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					EditText field = (EditText) v;
					if (!hasFocus)
					{
						Cart cart = CartManager.getInstance().getActiveCart();
						// Update only if changed
						String newVal = field.getText().toString();
						BigDecimal oldVal = cart.getDiscount();
						if ( !newVal.isEmpty() && !newVal.equals( oldVal.toString()) ) 
						{
							try {
								cart.setDiscount( new BigDecimal( newVal ) );
							} catch (NumberFormatException e) { }
						} 
						field.setText( NumberFormat.getCurrencyInstance().format(oldVal) );
						hideKeyboard();
					} 
					else {
						clearFieldShowKeyboard( field );
					}
				}
			});

		// Handle applied discount percent
		discountPct_.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if ( actionId == EditorInfo.IME_ACTION_DONE ) {
						v.clearFocus();
					}
					return false;
				}
			});

		discountPct_.setOnFocusChangeListener( new View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					EditText field = (EditText) v;
					if (!hasFocus)
					{
						Cart cart = CartManager.getInstance().getActiveCart();
						// Update only if changed
						String newValStr = field.getText().toString();
						if ( !newValStr.isEmpty() )
						{
							try {
								cart.setDiscountPct( new BigDecimal( newValStr ).divide(Conf.BIG_HUNDRED) );
							} catch (NumberFormatException e) { }
						} 
						field.setText( NumberFormat.getPercentInstance().format(cart.getDiscountPct()) );
						hideKeyboard();
					} 
					else {
						clearFieldShowKeyboard( field );
					}
				}
			});

		// Button: Go Back
		Button goBackBtn = (Button) cartLayout.findViewById(R.id.checkout_cart_go_back_button);
		goBackBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});


		ListView cartItemList = (ListView) cartLayout.findViewById(R.id.checkout_cart_list);
		// Keep in focus
		cartItemList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		cartItemList.setAdapter(adapter_);

		return cartLayout;
	}

	private void hideKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(tabName_.getWindowToken(), 0);
		tabName_.requestFocus();
	}

	private void clearFieldShowKeyboard(EditText v)
	{
		v.setText("");
		v.requestFocus();
		InputMethodManager manager= (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
	}

	private void onCartContentChanged( CartManager.Entry cartHolder )
	{
		// Update totals, amount dues	
		onTransactionStatusChanged( cartHolder );
		// reset cart name, hide soft keyboard
		tabName_.setText( cartHolder.name );
		adapter_.refreshView();
	}

	private void onTransactionStatusChanged( CartManager.Entry cartHolder )
	{
		// Update displayed values
		cartTotal_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.totalValue() ) );
		chargeAmount_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.getChargeAmount() ) );
		amountDue_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.amountDue() ) );
		discountValue_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.getDiscount() ) );
		discountPct_.setText( NumberFormat.getPercentInstance().format( cartHolder.cart.getDiscountPct() ) );

		// Turn on/off buttons depending on cart state.
		if ( cartHolder.cart.isPaid() )
		{
			chargeAmountLabel_.setVisibility( View.GONE );
			chargeAmount_.setVisibility( View.GONE );
			amountDueLabel_.setVisibility( View.GONE );
			amountDue_.setVisibility( View.GONE );
			discountLabel_.setVisibility( View.GONE );
			discountValue_.setVisibility( View.GONE );
			discountPct_.setVisibility( View.GONE );
			cartPaidOffLabel_.setVisibility( View.VISIBLE );
			Util.enableButton(btnFinished_, R.drawable.btn_green, btnTextColorOn_);
		}
		else // cart not paid in full
		{
			chargeAmountLabel_.setVisibility( View.VISIBLE );
			chargeAmount_.setVisibility( View.VISIBLE );
			amountDueLabel_.setVisibility( View.VISIBLE );
			amountDue_.setVisibility( View.VISIBLE );
			discountLabel_.setVisibility( View.VISIBLE );
			discountValue_.setVisibility( View.VISIBLE );
			discountPct_.setVisibility( View.VISIBLE );
			cartPaidOffLabel_.setVisibility( View.GONE );
			// Keep button enabled, but gray it out to signify 
			// not an ideal finishing stage
			Util.enableButton(btnFinished_, R.drawable.btn_gray, btnTextColorOff_);
		}
	}

	private CartEntryArrayAdapter adapter_;
	private TextView cartTotal_;
	private TextView tabName_;
	private TextView chargeAmountLabel_;
	private EditText chargeAmount_;
	private TextView amountDueLabel_;
	private TextView amountDue_;

	private TextView discountLabel_;
	private TextView discountValue_;
	private TextView discountPct_;
	private Button btnFinished_;
	private TextView cartPaidOffLabel_;
	private Handler handler_;
	private int btnTextColorOn_;
	private int btnTextColorOff_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<CheckoutCartFragment> ref_; 

		IncomingHandler(CheckoutCartFragment ref) {
			ref_ = new WeakReference<CheckoutCartFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			CheckoutCartFragment ref = ref_.get();
			if (ref != null)
			{
				switch( msg.what )
				{
					case MessageId.CART_CONTENT_CHANGED:
					ref.onCartContentChanged( CartManager.getInstance().getActive() );
					break;

					case MessageId.TRANSACTION_STATUS_CHANGED:
					ref.onTransactionStatusChanged( CartManager.getInstance().getActive() );
					break;
				}
			}
		}
	}
}
