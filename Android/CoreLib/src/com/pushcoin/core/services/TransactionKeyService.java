package com.pushcoin.core.services;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.pushcoin.core.alarms.TransactionKeyAlarm;
import com.pushcoin.core.utils.Logger;

public class TransactionKeyService extends WakefulIntentService {

	public static void scheduleAlarms(Context ctxt, int nextAlarmInMillis,
			boolean force) {
		WakefulIntentService.scheduleAlarms(new TransactionKeyAlarm(
				nextAlarmInMillis), ctxt, force);
	}

	private static Logger log = Logger.getLogger(TransactionKeyService.class);

	public TransactionKeyService() {
		super("TransactionKeyService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		log.d("Key Fetching; ");
		TransactionKeyService.scheduleAlarms(this, 5000, false);
	}
}
