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

public class EnterRegistrationCodeFragment 
	extends Fragment
{
	public interface OnSubmitRegistrationCode 
	{
		void onSubmitRegistrationCode( String code );
	}

	EnterRegistrationCodeFragment( OnSubmitRegistrationCode handler ) {
		handler_ = handler;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.enter_registration_code_tab, container, false);

		// Reg code input field
		final EditText regCodeInput = (EditText) fragmentRootLayout.findViewById(R.id.info_step_input_reg_code);

		// When code is entered, go out and register this app
		regCodeInput.setOnEditorActionListener(
			new EditText.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (actionId == EditorInfo.IME_ACTION_SEARCH ||
						actionId == EditorInfo.IME_ACTION_DONE ||
						event.getAction() == KeyEvent.ACTION_DOWN &&
						event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
					{
						String code = v.getText().toString();
						if ( !code.isEmpty() )
						{
							// the user is done typing. 
							handler_.onSubmitRegistrationCode( code );
						}
					}
					return false; // pass on to other listeners. 
				}
			});

		return fragmentRootLayout;
	}

	final OnSubmitRegistrationCode handler_;
}
