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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.os.Bundle;
import android.content.DialogInterface;

public class ClearCartDialog extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.confirm_clear_cart)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id) {
						CartManager.getInstance().getActiveCart().clear();
					}
				})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) { 
						// no-op, without it Cancel is not offered
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	public static void showDialog(FragmentManager fm)
	{
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag( Conf.DIALOG_CONFIRM_CLEAR_CART );
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = new ClearCartDialog();
		newFragment.show(ft, Conf.DIALOG_CONFIRM_CLEAR_CART);
	}
}
