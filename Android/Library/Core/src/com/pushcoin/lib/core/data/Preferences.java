/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
