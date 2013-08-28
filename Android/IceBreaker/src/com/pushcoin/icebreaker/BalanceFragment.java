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

public class BalanceFragment 
	extends Fragment
	implements ModelEventHandler
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.balance_tab, container, false);

		// lookup fields
		statusField_ = (TextView) fragmentRootLayout.findViewById(R.id.status_field);
		ratingBar_ = (RatingBar) fragmentRootLayout.findViewById(R.id.recent_transaction_rating_bar);
		ratingBarLabel_ = (TextView) fragmentRootLayout.findViewById(R.id.recent_transaction_rating_label);

		// access resources
		Resources res = getResources();

		// rating scale
		ratingScale_ = res.getStringArray(R.array.rating_scale);
		// call to action -- rate now
		ratingBarLabel_.setText(ratingScale_[0]);
		ratingBar_.setOnRatingBarChangeListener( new RatingBar.OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar b, float rating, boolean fromUser) 
			{
				// change label
				ratingBarLabel_.setText(ratingScale_[ (int) Math.round(rating) ]);

				// send to backend
				// TODO...
			}
		});

		return fragmentRootLayout;
	}

	// Invoked by the Controller
	@Override
	public void onAccountHistoryChanged()
	{
		// update this fragment's view
	}

	// Cached widget views
	TextView statusField_;
	RatingBar ratingBar_;
	TextView ratingBarLabel_;

	String[] ratingScale_;
}
