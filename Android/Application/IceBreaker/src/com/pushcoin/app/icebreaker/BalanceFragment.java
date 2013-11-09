package com.pushcoin.icebreaker;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.res.Resources;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RatingBar;
import android.text.Html;

public class BalanceFragment 
	extends Fragment
{
	public BalanceFragment(Controller ctrl)
	{
		ctrl_ = ctrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		ctrl_.registerHandler( handler_, MessageId.MODEL_CHANGED );
		ctrl_.registerHandler( handler_, MessageId.STATUS_CHANGED );

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.balance_tab, container, false);

		// lookup fields
		titleField_ = (TextView) fragmentRootLayout.findViewById(R.id.page_title);
		balanceField_ = (TextView) fragmentRootLayout.findViewById(R.id.account_balance_field);
		balanceTimeField_ = (TextView) fragmentRootLayout.findViewById(R.id.account_balance_time_field);
		statusField_ = (TextView) fragmentRootLayout.findViewById(R.id.status_field);
		recentTransactionUserRatingBar_ = (RatingBar) fragmentRootLayout.findViewById(R.id.recent_transaction_rating_bar);
		recentTransactionUserRatingBarLabel_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_rating_label);
		recentTransactionMerchantScore_ = (RatingBar) fragmentRootLayout.findViewById(R.id.merchant_score_bar);
		recentTransactionMerchantScoreLabel_ = (TextView) fragmentRootLayout.findViewById(R.id.merchant_score_label);
		recentTransactionPanel_ = fragmentRootLayout.findViewById(R.id.recent_transaction_panel);

		recentTransactionBusinessName_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_business_name_field);
		recentTransactionPaymentInfo_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_payment_info);
		recentTransactionAddressStreet_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_street_field);
		recentTransactionAddressCityStateZip_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_city_state_zip_field);
		recentTransactionAddressPhone_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_phone_field);

		// access resources
		Resources res = getResources();

		// rating scale
		ratingScale_ = res.getStringArray(R.array.rating_scale);
		// call to action -- rate now
		recentTransactionUserRatingBarLabel_.setText(ratingScale_[0]);
		recentTransactionUserRatingBar_.setOnRatingBarChangeListener( new RatingBar.OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar b, float rating, boolean fromUser) 
			{
				int roundedRating = (int) Math.round(rating);
				// change label
				recentTransactionUserRatingBarLabel_.setText(ratingScale_[ roundedRating ]);

				if (fromUser) // send user vote 
				{
					PcosHelper.TransactionInfo mostRecent = ctrl_.getRecentTransaction();
					if (mostRecent != null) {
						ctrl_.castRating( mostRecent.txnId, roundedRating );
					}
				}
			}
		});

		// Hide recent transaction view if there is nothing to show
		if (ctrl_.getRecentTransaction() == null) {
			recentTransactionPanel_.setVisibility(View.INVISIBLE);
		} else {
			onAccountHistoryChanged();
		}

		return fragmentRootLayout;
	}

	/** Dispatch events. */
	private Handler handler_ = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch( msg.what )
			{
				case MessageId.MODEL_CHANGED:
					onAccountHistoryChanged();
				break;
				case MessageId.STATUS_CHANGED:
					onStatusChanged();
				break;
			}
		}
	};

	public void onStatusChanged()
	{
		String status = ctrl_.getStatus();
		if (status.isEmpty()) {
			statusField_.setVisibility(View.INVISIBLE);
		}
		else {
			statusField_.setText( status );
			statusField_.setVisibility(View.VISIBLE);
		}
	}

	public void onAccountHistoryChanged()
	{
		// update this fragment's view
		titleField_.setText( ctrl_.getPageTitle() );
		balanceField_.setText( ctrl_.getBalance() );
		balanceTimeField_.setText( ctrl_.getBalanceTime() );

		// show status if necesery
		onStatusChanged();

		// Show most recent transaction
		PcosHelper.TransactionInfo mostRecent = ctrl_.getRecentTransaction();
		if (mostRecent != null)
		{
			String transactionType;
			if (mostRecent.txnType.equals(Conf.TRANSACTION_TYPE_DEBIT)) {
				transactionType = "paid";
			} else if (mostRecent.txnType.equals(Conf.TRANSACTION_TYPE_CREDIT)){
				transactionType = "received";
			}
			else {
				transactionType = " transacted "; 
			}

			String deviceName;
			if (mostRecent.deviceName.isEmpty()) {
				deviceName = "You";
			} else {
				deviceName = mostRecent.deviceName;
			}

			// transaction rating only if we paid to a merchant
			boolean showRatingBar = false;
			if (mostRecent.txnType.equals(Conf.TRANSACTION_TYPE_DEBIT) && !mostRecent.counterParty.isEmpty())
			{
				recentTransactionUserRatingBar_.setRating(mostRecent.txnRating);
				// turn off voting if transaction took place more than CUTOFF ago
				long nowEpoch = PcosHelper.getEpochUtc();
				if (nowEpoch < mostRecent.txnTimeEpoch || (nowEpoch - mostRecent.txnTimeEpoch) < Conf.USER_RATING_CUTOFF)
				{
					showRatingBar = true;
					recentTransactionUserRatingBar_.setIsIndicator( false );
					recentTransactionUserRatingBarLabel_.setText(ratingScale_[ mostRecent.txnRating ]);
				}
				else // voting right expired
				{
					recentTransactionUserRatingBar_.setIsIndicator( true );
					// if user voted, we show how
					if (mostRecent.txnRating > 0)
					{
						showRatingBar = true;
						recentTransactionUserRatingBarLabel_.setText(ratingScale_[ mostRecent.txnRating ]);
					}
				}
			}

			recentTransactionUserRatingBar_.setVisibility(showRatingBar ? View.VISIBLE : View.INVISIBLE);
			recentTransactionUserRatingBarLabel_.setVisibility(showRatingBar ? View.VISIBLE : View.INVISIBLE);

			// business name, address, rating
			if (mostRecent.counterParty.isEmpty() ) {
				recentTransactionBusinessName_.setText( mostRecent.note );
			} else {
				recentTransactionBusinessName_.setText( mostRecent.counterParty );
			}

			recentTransactionPaymentInfo_.setText(Html.fromHtml( deviceName + " " + transactionType + " <b>" + PcosHelper.prettyAmount( mostRecent.amount, mostRecent.currency ) + "</b> " + PcosHelper.prettyTime(getActivity(), mostRecent.txnTimeEpoch)) );
			recentTransactionAddressStreet_.setText( mostRecent.posAddress.street );
			recentTransactionAddressCityStateZip_.setText(mostRecent.posAddress.city + ", " + mostRecent.posAddress.state + " " + mostRecent.posAddress.zipCode);
			String merchantPhone;
			if ( !mostRecent.merchantPhone.isEmpty() ) {
				merchantPhone = "Ph: " + mostRecent.merchantPhone;
			} else {
				merchantPhone = "";
			}
			recentTransactionAddressPhone_.setText( merchantPhone );
			if (mostRecent.totalVotes > 0) 
			{
				recentTransactionMerchantScore_.setRating(mostRecent.merchantScore);		
				recentTransactionMerchantScoreLabel_.setText( String.format("Score %.1f / 5 (%d votes)", mostRecent.merchantScore, mostRecent.totalVotes) );
				recentTransactionMerchantScore_.setVisibility(View.VISIBLE);		
				recentTransactionMerchantScoreLabel_.setVisibility(View.VISIBLE);
			}
			else
			{
				recentTransactionMerchantScore_.setVisibility(View.INVISIBLE);		
				recentTransactionMerchantScoreLabel_.setVisibility(View.INVISIBLE);
			}

			recentTransactionPanel_.setVisibility(View.VISIBLE);
		} 
		else {
			recentTransactionPanel_.setVisibility(View.INVISIBLE);
		}
	}

	final Controller ctrl_;

	// Cached widget views
	TextView titleField_;
	TextView balanceField_;
	TextView balanceTimeField_;
	TextView statusField_;
	RatingBar recentTransactionUserRatingBar_;
	TextView recentTransactionUserRatingBarLabel_;
	RatingBar recentTransactionMerchantScore_;
	TextView recentTransactionMerchantScoreLabel_;
	View recentTransactionPanel_;

	TextView recentTransactionBusinessName_;
	TextView recentTransactionPaymentInfo_;
	TextView recentTransactionAddressStreet_;
	TextView recentTransactionAddressCityStateZip_;
	TextView recentTransactionAddressPhone_;

	String[] ratingScale_;
}
