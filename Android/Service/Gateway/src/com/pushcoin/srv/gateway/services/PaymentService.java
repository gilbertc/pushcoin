package com.pushcoin.srv.gateway.services;

import java.util.Date;

import com.pushcoin.lib.core.data.DemoChallenge;
import com.pushcoin.lib.core.data.DisplayParcel;
import com.pushcoin.lib.core.data.IChallenge;
import com.pushcoin.lib.core.data.Preferences;
import com.pushcoin.lib.core.data.TransactionKey;
import com.pushcoin.lib.core.devices.DeviceManager;
import com.pushcoin.lib.core.payment.IPayment;
import com.pushcoin.lib.core.payment.PaymentListener;
import com.pushcoin.lib.core.query.IQuery;
import com.pushcoin.lib.core.query.QueryListener;
import com.pushcoin.lib.core.utils.Logger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class PaymentService extends Service implements PaymentListener,
		QueryListener {
	private static Logger log = Logger.getLogger(PaymentService.class);

	public static final String ACTION_START = "com.pushcoin.srv.gateway.services.PaymentService.START";
	public static final String ACTION_STOP = "com.pushcoin.srv.gateway.services.PaymentService.STOP";
	public static final String ACTION_DISPLAY = "com.pushcoin.srv.gateway.services.PaymentService.DISPLAY";
	public static final String KEY_DISPLAYPARCEL = "DisplayParcel";
	public static final String KEY_MESSENGER = "Messenger";
	public static final String KEY_REQUEST_ID = "RequestId";

	public static final int MSGID_ERROR = -1;
	public static final int MSGID_COMPLETE = 1;
	public static final int MSGID_QUERY = 2;

	private DeviceManager deviceManager;
	private Messenger messenger;
	private int startId = -1;

	public PaymentService() {
		log.d("constructor");
	}

	@Override
	public void onCreate() {
		log.d("onCreate");
		super.onCreate();
		UsbManager usbManager = (UsbManager) this
				.getSystemService(Context.USB_SERVICE);
		this.deviceManager = DeviceManager.createDefault(this, usbManager);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		this.startId = startId;
		onHandleIntent(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log.d("onStartCommand");
		onStart(intent, startId);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		log.d("onDestroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		log.d("onBind");
		return null;
	}

	protected void onHandleIntent(Intent intent) {
		log.d(intent.getAction());
		if (intent.getAction() == ACTION_START) {
			Bundle bundle = intent.getExtras();
			start(bundle);
		} else if (intent.getAction() == ACTION_STOP) {
			Bundle bundle = intent.getExtras();
			stop(bundle);
		} else if (intent.getAction() == ACTION_DISPLAY) {
			Bundle bundle = intent.getExtras();
			display(bundle);
		} else {
			log.d("not supported action: " + intent.getAction());
		}
	}

	private void start(Bundle bundle) {
		log.d("start");

		if (bundle == null) {
			log.e("bundle null");
		} else {
			try {
				this.messenger = (Messenger) bundle.get(KEY_MESSENGER);
				this.deviceManager.enablePayment(this);
				this.deviceManager.enableQuery(this);
			} catch (Exception ex) {
				log.e("start", ex);
			}
		}
	}

	private void stop(Bundle bundle) {
		log.d("stop");

		this.deviceManager.disableAll();
		stopSelf(this.startId);
	}

	private void display(Bundle bundle) {
		log.d("display");

		try {
			DisplayParcel displayParcel = bundle != null ? (DisplayParcel) bundle
					.getSerializable(KEY_DISPLAYPARCEL) : null;
			this.deviceManager.display(displayParcel);
		} catch (Exception ex) {
			log.e("display", ex);
		}
	}

	private IChallenge getChallenge() {

		if (Preferences.isDemoMode(this, false)) {
			return new DemoChallenge();
		}

		TransactionKey[] keys = TransactionKey.getKeys();

		if (keys != null) {
			Date now = new Date();
			for (TransactionKey key : keys) {
				if (key.expire.after(now))
					return key.getChallenge();
			}
		}
		log.e("no transaction keys");
		return null;
	}

	@Override
	public void onPaymentDiscovered(IPayment tech) {
		Message msg = Message.obtain();

		try {
			tech.connect();

			msg.what = MSGID_COMPLETE;
			msg.obj = tech.getMessage(getChallenge());

			tech.close();

		} catch (Exception e) {
			log.d("Service Error", e);
			msg.what = MSGID_ERROR;
			msg.obj = e;
		}

		try {
			this.messenger.send(msg);
		} catch (Exception e) {
			log.d("Service", e);
		}

	}

	@Override
	public void onQueryDiscovered(IQuery query) {
		Message msg = Message.obtain();
		msg.what = MSGID_QUERY;
		msg.obj = query;

		try {
			this.messenger.send(msg);
		} catch (Exception e) {
			log.d("Service", e);
		}
	}

}
