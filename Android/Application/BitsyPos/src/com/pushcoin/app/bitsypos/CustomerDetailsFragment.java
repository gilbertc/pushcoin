package com.pushcoin.app.bitsypos;

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
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.lang.ref.WeakReference;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// User-details layout
		rootView_ = inflater.inflate(R.layout.customer_details_layout, container, false);
		// Cached widgets
		firstName_ = (TextView) rootView_.findViewById(R.id.customer_details_fname);
		lastName_ = (TextView) rootView_.findViewById(R.id.customer_details_lname);
		title_ = (TextView) rootView_.findViewById(R.id.customer_details_title);
		identifier_ = (TextView) rootView_.findViewById(R.id.customer_details_customer_identifier);
		balance_ = (TextView) rootView_.findViewById(R.id.customer_details_customer_balance);
		mugshot_ = (ImageView) rootView_.findViewById(R.id.customer_details_mugshot);

		return rootView_;
	}
	
	private void onCustomerDetailsAvailable( Customer user )
	{
		firstName_.setText( user.firstName );
		lastName_.setText( user.lastName );
		title_.setText( user.title );
		identifier_.setText( user.identifier );
		balance_.setText(  NumberFormat.getCurrencyInstance().format( user.balance ) );
		mugshot_.setImageBitmap( user.mugshot );
		rootView_.setVisibility(View.VISIBLE);
	}
	
	private void onQueryUsersReply( Message msg )
	{
		List<Customer> customers = (List<Customer>) msg.obj;
		if (customers.size() == 1) {
			onCustomerDetailsAvailable( customers.get(0) ); 
		}
	}

	private Handler handler_;
	// UI with user info
	private View rootView_;
	private TextView firstName_;
	private TextView lastName_;
	private TextView title_;
	private TextView identifier_;
	private TextView balance_;
	private ImageView mugshot_;

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

				}
			}
		}
	}
}
