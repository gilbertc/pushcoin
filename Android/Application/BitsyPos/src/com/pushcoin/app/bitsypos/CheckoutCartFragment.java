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
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "CheckoutCartFragment" );

		// Restore cart contents
		onCartContentChanged();
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
			R.layout.shopping_cart_row,
			R.id.shopping_cart_entry_title,
			R.id.shopping_cart_entry_price,
			R.id.shopping_cart_entry_note);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.checkout_cart, container, false);

		// Cache cart widgets
		cartTotal_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_total);
		tabName_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_tab_name);
		chargeAmount_ = (EditText) cartLayout.findViewById(R.id.checkout_cart_charge_amount);
		amountDue_ = (TextView) cartLayout.findViewById(R.id.checkout_cart_due_amount);

		// Handle "Read Card" click
		Button readCard = (Button) cartLayout.findViewById(R.id.checkout_cart_read_card_button);
		readCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppDb.getInstance().asyncFindCustomerWithKeyword( getActivity(), "one", EventHub.getInstance() );
			}
		});

		ListView cartItemList = (ListView) cartLayout.findViewById(R.id.checkout_cart_list);
		// Keep in focus
		cartItemList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		cartItemList.setAdapter(adapter_);

		return cartLayout;
	}

	private void onCartContentChanged()
	{
		// Update cart total, owning balance
		CartManager.Entry cartHolder = CartManager.getInstance().getActiveEntry();
		cartTotal_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.totalValue() ) );
		chargeAmount_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.amountDue() ) );
		amountDue_.setText( NumberFormat.getCurrencyInstance().format( cartHolder.cart.amountDue() ) );
		// cart name might have changed too -- on tab change
		tabName_.setText( cartHolder.name );

		adapter_.refreshView();
	}

	private CartEntryArrayAdapter adapter_;
	private TextView cartTotal_;
	private TextView tabName_;
	private EditText chargeAmount_;
	private TextView amountDue_;
	private Handler handler_;

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
					case MessageId.CART_POOL_CHANGED:
					case MessageId.CART_CONTENT_CHANGED:
						ref.onCartContentChanged();
					break;
				}
			}
		}
	}
}
