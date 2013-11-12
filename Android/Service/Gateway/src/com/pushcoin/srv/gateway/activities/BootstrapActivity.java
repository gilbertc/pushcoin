package com.pushcoin.srv.gateway.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.pushcoin.srv.gateway.R;
import com.pushcoin.srv.gateway.services.TransactionKeyService;
import com.pushcoin.ifce.connect.Actions;
import com.pushcoin.ifce.connect.Keys;
import com.pushcoin.ifce.connect.data.Result;
import com.pushcoin.lib.core.data.Preferences;
import com.pushcoin.lib.core.net.Server;
import com.pushcoin.lib.core.security.KeyStore;
import com.pushcoin.lib.core.utils.Logger;

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

		if (getIntent().getAction() == null
				|| !getIntent().getAction().endsWith(Actions.ACTION_BOOTSTRAP)) {
			log.d("launched by user");

			KeyStore keyStore = KeyStore.getInstance(this);
			if (!keyStore.hasMAT()) {
				log.d("cannot find mat");
				Intent myIntent = new Intent(this, RegisterDeviceActivity.class);
				startActivity(myIntent);

			} else {
				TransactionKeyService.scheduleAlarms(this, 0, false);
				Toast.makeText(this, getString(R.string.transaction_key_obtained), Toast.LENGTH_LONG).show();

				Intent myIntent = new Intent(this, SettingsActivity.class);
				startActivity(myIntent);
			}
		} else {
			log.d("launched by app");
			KeyStore keyStore = KeyStore.getInstance(this);
			if (!keyStore.hasMAT()) {
				log.d("cannot find mat");
				Intent myIntent = new Intent(this, RegisterDeviceActivity.class);
				startActivityForResult(myIntent, 0);
			} else {
				TransactionKeyService.scheduleAlarms(this, 0, false);
				Toast.makeText(this, getString(R.string.transaction_key_obtained), Toast.LENGTH_LONG).show();

				Intent returnIntent = new Intent();
				Result result = new Result();
				result.type = Result.TYPE_BOOTSTRAP;
				returnIntent.putExtra(Keys.KEY_RESULT, result);
				setResult(Result.RESULT_OK, returnIntent);
				finish();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		log.i("Result = " + resultCode);
		setResult(resultCode, intent);
		finish();
	}
}
