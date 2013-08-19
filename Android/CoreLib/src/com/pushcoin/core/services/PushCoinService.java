package com.pushcoin.core.services;

import com.pushcoin.core.data.Challenge;
import com.pushcoin.core.data.DisplayParcel;
import com.pushcoin.core.devices.DeviceManager;
import com.pushcoin.core.payment.IPayment;
import com.pushcoin.core.payment.PaymentListener;
import com.pushcoin.core.utils.Logger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class PushCoinService extends Service implements PaymentListener {
	private static Logger log = Logger.getLogger(PushCoinService.class);

	public static final String ACTION_START = "com.pushcoin.core.intent.actions.START";
	public static final String ACTION_STOP = "com.pushcoin.core.intent.actions.STOP";
	public static final String ACTION_DISPLAY = "com.pushcoin.core.intent.actions.DISPLAY";
	public static final String KEY_AMOUNT = "Amount";
	public static final String KEY_DISPLAYPARCEL = "DisplayParcel";
	public static final String KEY_MESSENGER = "Messenger";

	public static final int MSGID_COMPLETE = 1;

	private DeviceManager deviceManager;
	private String amount;
	private Messenger messenger;
	private int startId = -1;

	public PushCoinService() {
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
			if (bundle != null)
				start(bundle);
			else
				log.d("bundle null");
		} else if (intent.getAction() == ACTION_STOP) {
			Bundle bundle = intent.getExtras();
			if (bundle != null)
				stop(bundle);
			else
				log.d("bundle null");
		} else if (intent.getAction() == ACTION_DISPLAY) {
			Bundle bundle = intent.getExtras();
			if (bundle != null)
				display(bundle);
			else
				log.d("bundle null");
		} else {
			log.d("not supported action: " + intent.getAction());
		}
	}

	private void start(Bundle bundle) {
		log.d("start");

		try {
			this.messenger = (Messenger) bundle.get(KEY_MESSENGER);
			this.amount = bundle.getString(KEY_AMOUNT);
			this.deviceManager.enable(this);
		} catch (Exception ex) {
			log.e("start", ex);
		}
	}

	private void stop(Bundle bundle) {
		log.d("stop");

		this.deviceManager.disable();
		stopSelf(this.startId);
	}

	private void display(Bundle bundle) {
		log.d("display");
		try {
			DisplayParcel displayParcel = (DisplayParcel) bundle
					.getSerializable(KEY_DISPLAYPARCEL);
			this.deviceManager.display(displayParcel);
		} catch (Exception ex) {
			log.e("display", ex);
		}
	}

	private Challenge getChallenge() {
		return null;
	}

	@Override
	public void onPaymentDiscovered(IPayment tech) {
		byte[] blob;

		try {
			tech.connect();

			blob = tech.getMessage(getChallenge());

			tech.close();

		} catch (Exception e) {
			log.d("Service", e);
		}

		Message msg = Message.obtain();
		msg.what = MSGID_COMPLETE;
		try {
			this.messenger.send(msg);
		} catch (Exception e) {
			log.d("Service", e);
		}

	}

}
