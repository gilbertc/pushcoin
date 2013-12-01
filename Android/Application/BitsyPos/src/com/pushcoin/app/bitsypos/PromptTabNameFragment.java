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

import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.app.Fragment;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.EditText;
import android.widget.Button;

public class PromptTabNameFragment extends DialogFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context context = getActivity();
		getDialog().setTitle( getResources().getString(R.string.prompt_tab_name) );

		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.prompt_tab_name_dialog, container, false);

		// Set initial name, set focus
		final EditText tabName = (EditText) rootView.findViewById(R.id.prompt_tab_name_editor);
		tabName.requestFocus();

		// Install Done and Cancel handlers
		final Button doneBtn = (Button) rootView.findViewById( R.id.prompt_tab_name_done_button );
		doneBtn.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					String newTabName = tabName.getText().toString().trim();
					if (! newTabName.isEmpty() ) 
					{
						CartManager.getInstance().create( newTabName, true );
						((SlidingActivity)getActivity()).getSlidingMenu().showContent();
					}
					dismiss();
				}
			});

		final Button cancelBtn = (Button) rootView.findViewById( R.id.prompt_tab_name_cancel_button );
		cancelBtn.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v) {
					dismiss();
				}
			});


		return rootView;
	}

	public static void showDialog(FragmentManager fm)
	{
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag( Conf.DIALOG_PROMPT_TAB_NAME );
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = new PromptTabNameFragment();
		newFragment.show(ft, Conf.DIALOG_PROMPT_TAB_NAME);
	}
}
