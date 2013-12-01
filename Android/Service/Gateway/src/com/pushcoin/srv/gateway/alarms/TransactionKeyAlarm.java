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

package com.pushcoin.srv.gateway.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.pushcoin.srv.gateway.services.TransactionKeyService;

public class TransactionKeyAlarm implements WakefulIntentService.AlarmListener {

	long nextAlarmInMillis;

	public TransactionKeyAlarm() {

	}

	public TransactionKeyAlarm(long nextAlarmInMillis) {
		this.nextAlarmInMillis = nextAlarmInMillis;
	}

	public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context ctxt) {
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + nextAlarmInMillis, pi);
	}

	public void sendWakefulWork(Context ctxt) {
		WakefulIntentService.sendWakefulWork(ctxt, TransactionKeyService.class);
	}

	public long getMaxAge() {
		return 0;
	}
}
