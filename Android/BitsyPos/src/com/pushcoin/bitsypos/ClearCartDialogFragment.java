package com.pushcoin.bitsypos;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.os.Bundle;
import android.content.DialogInterface;

public class ClearCartDialogFragment extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.confirm_clear_cart)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id) 
					{
						Cart cart = (Cart) SessionManager.getInstance().get( Conf.SESSION_KEY_CART );
						cart.clear();
					}
				})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id) {
					// User cancelled the dialog
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
		DialogFragment newFragment = new ClearCartDialogFragment();
		newFragment.show(ft, Conf.DIALOG_CONFIRM_CLEAR_CART);
	}
}
