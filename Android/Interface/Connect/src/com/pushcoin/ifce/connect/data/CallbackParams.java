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

package com.pushcoin.ifce.connect.data;

import android.os.Bundle;
import android.os.Messenger;

public class CallbackParams implements Bundlable {
	public static final String KEY_MESSENGER = "MESSENGER";
	public static final String KEY_CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";

	protected Bundle bundle;

	public CallbackParams(Bundle bundle) {
		this.bundle = bundle;
		this.bundle.setClassLoader(Thread.currentThread()
				.getContextClassLoader());
	}

	public Messenger getMessenger() {
		return bundle.getParcelable(KEY_MESSENGER);
	}

	public void setMessenger(Messenger value) {
		bundle.putParcelable(KEY_MESSENGER, value);
	}

	public Bundle getBundle() {
		return bundle;
	}

	public String getClientRequestId() {
		return bundle.getString(KEY_CLIENT_REQUEST_ID, "");
	}

	public void setClientRequestId(String value) {
		bundle.putString(KEY_CLIENT_REQUEST_ID, value);
	}

}
