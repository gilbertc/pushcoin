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

		SharedPreferences prefs = Preferences.get(this);

		Editor editor = prefs.edit();
		if (!prefs.contains(Preferences.PREF_URL)) {
			editor.putString(Preferences.PREF_URL,
					getString(R.string.url_pushcoin_prod));
		}
		if (!prefs.contains(Preferences.PREF_DEMO_MODE)) {
			editor.putBoolean(Preferences.PREF_DEMO_MODE, false);
		}
		if (!prefs.contains(Preferences.PREF_OFFLINE_MODE)) {
			editor.putBoolean(Preferences.PREF_OFFLINE_MODE, false);
		}
		editor.commit();

		Server.setDefaultUrl(Preferences.url(this,
				getString(R.string.url_pushcoin_prod)));

		if (getIntent().getAction() == null
				|| !getIntent().getAction().endsWith(Actions.ACTION_BOOTSTRAP)) {
			log.d("launched by user");

			if (Preferences.isDemoMode(this, false)) {
				Intent myIntent = new Intent(this, SettingsActivity.class);
				startActivity(myIntent);
			} else {
				if (!KeyStore.getInstance(this).hasMAT()) {
					log.d("cannot find mat");
					Intent myIntent = new Intent(this,
							RegisterDeviceActivity.class);
					startActivity(myIntent);

				} else {
					TransactionKeyService.scheduleAlarms(this, 0, false);
					Toast.makeText(this,
							getString(R.string.transaction_key_obtained),
							Toast.LENGTH_LONG).show();

					Intent myIntent = new Intent(this, SettingsActivity.class);
					startActivity(myIntent);
				}
			}
		} else {
			log.d("launched by app");
			if (Preferences.isDemoMode(this, false)) {
				Intent returnIntent = new Intent();
				Result result = new Result();
				result.type = Result.TYPE_BOOTSTRAP;
				returnIntent.putExtra(Keys.KEY_RESULT, result);
				setResult(Result.RESULT_OK, returnIntent);
				finish();
			} else {
				if (!KeyStore.getInstance(this).hasMAT()) {
					log.d("cannot find mat");
					Intent myIntent = new Intent(this,
							RegisterDeviceActivity.class);
					startActivityForResult(myIntent, 0);
				} else {
					TransactionKeyService.scheduleAlarms(this, 0, false);
					Toast.makeText(this,
							getString(R.string.transaction_key_obtained),
							Toast.LENGTH_LONG).show();

					Intent returnIntent = new Intent();
					Result result = new Result();
					result.type = Result.TYPE_BOOTSTRAP;
					returnIntent.putExtra(Keys.KEY_RESULT, result);
					setResult(Result.RESULT_OK, returnIntent);
					finish();
				}
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
