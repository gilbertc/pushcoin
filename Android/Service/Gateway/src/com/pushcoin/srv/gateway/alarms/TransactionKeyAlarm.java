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
