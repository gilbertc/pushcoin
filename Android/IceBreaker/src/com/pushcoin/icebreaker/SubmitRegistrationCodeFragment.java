package com.pushcoin.icebreaker;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.res.Resources;
import android.app.Fragment;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RatingBar;
import android.widget.ArrayAdapter;

public class SubmitRegistrationCodeFragment 
	extends Fragment
{
	public interface OnRegistrationProgress
	{
		void onUserCanceled();
	}

	SubmitRegistrationCodeFragment( OnRegistrationProgress handler ) {
		handler_ = handler;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.submitting_registration_code_tab, container, false);

		// User may have entered a bad reg. code -- let him retry
		final Button tryAgainButton = (Button) fragmentRootLayout.findViewById(R.id.try_again_button);
    tryAgainButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				handler_.onUserCanceled();
			}
		});

		return fragmentRootLayout;
	}
	
	final OnRegistrationProgress handler_;
}
