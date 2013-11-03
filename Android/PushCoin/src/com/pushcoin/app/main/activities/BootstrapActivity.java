package com.pushcoin.app.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.pushcoin.core.services.TransactionKeyService;

public class BootstrapActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * TransactionKeyService.scheduleAlarms(this, 5000, false);
		 * 
		 * Toast.makeText(this, "Transaction Key Service Active",
		 * Toast.LENGTH_LONG).show();
		 */
		// finish();
	}

	@Override
	public void onStart() {
		super.onStart();

		Intent myIntent = new Intent(this, RegisterDeviceActivity.class);
		startActivity(myIntent);
		finish();
	}

}
