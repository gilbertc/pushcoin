package com.pushcoin.lib.core.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	public static final String NAME = "com.pushcoin.lib.core";
	public static final String PREF_URL = "com.pushcoin.lib.core.PREF_URL";
	public static final String PREF_OFFLINE_MODE = "com.pushcoin.lib.core.PREF_OFFLINE";
	public static final String PREF_DEMO_MODE = "com.pushcoin.lib.core.PREF_DEMO";

	public static SharedPreferences get(Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}
	public static boolean isDemoMode(Context context, boolean defValue) {
		return get(context).getBoolean(Preferences.PREF_DEMO_MODE, defValue);
	}
	public static boolean isOfflineMode(Context context, boolean defValue) {
		return get(context).getBoolean(Preferences.PREF_OFFLINE_MODE, defValue);
	}
	public static String url(Context context, String defValue) {
		return get(context).getString(Preferences.PREF_URL, defValue);
	}
}
