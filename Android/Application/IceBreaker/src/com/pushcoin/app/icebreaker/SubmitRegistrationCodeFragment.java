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
import android.widget.ProgressBar;
import android.widget.TextView;

public class SubmitRegistrationCodeFragment 
	extends Fragment
{
	SubmitRegistrationCodeFragment( Controller ctrl ) {
		ctrl_ = ctrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		ctrl_.registerHandler( handler_, MessageId.REGISTER_DEVICE_PENDING );
		ctrl_.registerHandler( handler_, MessageId.REGISTER_DEVICE_STOPPED );

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.submitting_registration_code_tab, container, false);
		statusLabel_ = (TextView) fragmentRootLayout.findViewById(R.id.submitting_status);
		progressBar_ = (ProgressBar) fragmentRootLayout.findViewById(R.id.submitting_progress_bar);
		
		// Initially we don't show progress -- once we are notified that HTTP requset is pending
		// then we show it up.
		progressBar_.setVisibility(View.INVISIBLE);

		// User may have entered a bad reg. code -- let him retry
		final Button tryAgainButton = (Button) fragmentRootLayout.findViewById(R.id.try_again_button);
    tryAgainButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Message m = Message.obtain();
				m.what = MessageId.REGISTER_DEVICE_USER_CANCELED;
				ctrl_.post(m);
			}
		});

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
				case MessageId.REGISTER_DEVICE_PENDING:
					statusLabel_.setText( "Please wait..." );
					progressBar_.setVisibility(View.VISIBLE);
				break;
				case MessageId.REGISTER_DEVICE_STOPPED:
					statusLabel_.setText( ctrl_.getStatus() );
					progressBar_.setVisibility(View.INVISIBLE);
				break;
			}
		}
	};

	final Controller ctrl_;
	TextView statusLabel_;
	ProgressBar progressBar_;
}
