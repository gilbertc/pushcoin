package com.pushcoin.app.main.activities;

import com.pushcoin.app.main.R;
import com.pushcoin.app.main.R.layout;
import com.pushcoin.app.main.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ChargeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charge);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.charge, menu);
		return true;
	}

}
