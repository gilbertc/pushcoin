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
import java.util.ArrayList;

public class CheckoutFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View layout = inflater.inflate(R.layout.checkout_view, container, false);

		return layout;
	}
}
