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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RatingBar;
import android.widget.ArrayAdapter;

public class EnterRegistrationCodeFragment 
	extends Fragment
{
	EnterRegistrationCodeFragment( Controller ctrl ) {
		ctrl_ = ctrl;
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
							Message m = Message.obtain();
							m.what = MessageId.REGISTER_DEVICE_REQUEST;
							m.obj = code;
							ctrl_.post(m);
						}
					}
					return false; // pass on to other listeners. 
				}
			});

		return fragmentRootLayout;
	}

	final Controller ctrl_;
}
