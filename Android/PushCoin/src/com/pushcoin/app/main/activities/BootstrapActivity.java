package com.pushcoin.app.main.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.pushcoin.app.main.R;
import com.pushcoin.app.main.services.TransactionKeyService;
import com.pushcoin.core.interfaces.Preferences;
import com.pushcoin.core.net.Server;
import com.pushcoin.core.security.KeyStore;
import com.pushcoin.core.utils.Logger;

public class BootstrapActivity extends Activity {
	public static final String ACTION_MAIN = "android.intent.action.MAIN";
	private static Logger log = Logger.getLogger(BootstrapActivity.class);

	@Override
	public void onStart() {
		super.onStart();

		SharedPreferences prefs = getSharedPreferences(Preferences.NAME,
				Context.MODE_PRIVATE);

		if (!prefs.contains(Preferences.PREF_URL)) {
			Editor editor = prefs.edit();
			editor.putString(Preferences.PREF_URL,
					getString(R.string.url_pushcoin_prod));
			editor.commit();
		}

		Server.setDefaultUrl(prefs.getString(Preferences.PREF_URL,
				getString(R.string.url_pushcoin_prod)));

		KeyStore keyStore = KeyStore.getInstance(this);
		if (!keyStore.hasMAT()) {
			log.d("cannot find mat");
			Intent myIntent = new Intent(this, RegisterDeviceActivity.class);
			startActivity(myIntent);

		} else {
			TransactionKeyService.scheduleAlarms(this, 0, false);
			Toast.makeText(this, "Transaction Key Service Active",
					Toast.LENGTH_LONG).show();

			if (getIntent().getAction().compareTo(ACTION_MAIN) == 0) {
				log.d("launched by user");

				/* launched by user */
				Intent myIntent = new Intent(this, SettingsActivity.class);
				startActivity(myIntent);
			}
		}
		finish();

	}
}
