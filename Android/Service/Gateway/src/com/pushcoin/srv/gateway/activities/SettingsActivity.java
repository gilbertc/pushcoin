package com.pushcoin.srv.gateway.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import com.pushcoin.srv.gateway.R;
import com.pushcoin.srv.gateway.services.TransactionKeyService;
import com.pushcoin.lib.core.data.Preferences;
import com.pushcoin.lib.core.net.Server;
import com.pushcoin.lib.core.security.KeyStore;
import com.pushcoin.lib.core.utils.Logger;

public class SettingsActivity 
	extends PreferenceActivity 
	implements OnSharedPreferenceChangeListener
{
	private static Logger log = Logger.getLogger(SettingsActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		if ( item.getItemId() == R.id.action_reset_device )
		{
			showResetDeviceDialog();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	private AlertDialog showResetDeviceDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(getString(R.string.dialog_reset_device_title));
		dialog.setMessage(getString(R.string.dialog_reset_device_prompt));
		dialog.setPositiveButton(getString(R.string.dialog_reset_device_yes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						KeyStore keyStore = KeyStore.getInstance(SettingsActivity.this);
						if (keyStore != null) {
							keyStore.reset(SettingsActivity.this);
						}

						Toast.makeText(SettingsActivity.this, 
							getString(R.string.dialog_reset_device_toast), 
							Toast.LENGTH_LONG).show();

						Intent myIntent = new Intent(SettingsActivity.this, BootstrapActivity.class);
						startActivity(myIntent);
						SettingsActivity.this.finish();
					}
				});
		dialog.setNegativeButton(getString(R.string.dialog_reset_device_no),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
		return dialog.show();
	}

	@Override
	public void onBuildHeaders(List<Header> target)
	{
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = 
		new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference preference, Object value)
				{
					String stringValue = value.toString();

					if (preference instanceof ListPreference)
					{
						ListPreference listPreference = (ListPreference) preference;
						int index = listPreference.findIndexOfValue(stringValue);

						preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

					} else {
						preference.setSummary(stringValue);
					}
					return true;
				}
			};

	private static void bindPreferenceSummaryToValue(PreferenceManager preferenceManager, Preference preference)
	{
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		String value = preferenceManager.getSharedPreferences().getString(preference.getKey(), "");
		log.i("binding " + preference.getKey() + " value: " + value);
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(Preferences.NAME);
			getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
			addPreferencesFromResource(R.xml.pref_general);
			bindPreferenceSummaryToValue(getPreferenceManager(),
					findPreference(Preferences.PREF_URL));
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Preferences.PREF_URL))
		{
			String value = sharedPreferences.getString(key, "");
			log.d("preference changed: " + key + " check: " + value);

			Server.setDefaultUrl(value);
			KeyStore keyStore = KeyStore.getInstance(this);
			if (keyStore.hasMAT())
			{
				TransactionKeyService.scheduleAlarms(this, 0, false);
				Toast.makeText(this, "Transaction Key Service Active",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
