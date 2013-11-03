package com.pushcoin.app.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.pushcoin.core.security.KeyStore;
import com.pushcoin.core.services.TransactionKeyService;

public class BootstrapActivity extends Activity {
	public static final String ACTION_MAIN = "android.intent.action.MAIN";

	@Override
	public void onStart() {
		super.onStart();

		KeyStore keyStore = KeyStore.getInstance(this);
		if (!keyStore.hasMAT()) {
			Intent myIntent = new Intent(this, RegisterDeviceActivity.class);
			startActivity(myIntent);

		} else if (getIntent().getAction().compareTo(ACTION_MAIN) == 0) {

			/* launched by user */
			Intent myIntent = new Intent(this, SettingsActivity.class);
			startActivity(myIntent);

		} else {

			/* launched by apps, start the transactionKey service with our mat */
			TransactionKeyService.scheduleAlarms(this, 5000, false);
			Toast.makeText(this, "Transaction Key Service Active",
					Toast.LENGTH_LONG).show();
		}
		finish();

	}

}
