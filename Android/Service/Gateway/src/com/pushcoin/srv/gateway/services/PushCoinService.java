package com.pushcoin.srv.gateway.services;

import java.text.DecimalFormat;
import java.util.Date;

import com.pushcoin.ifce.connect.Actions;
import com.pushcoin.ifce.connect.Keys;
import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.Result;
import com.pushcoin.lib.core.data.DisplayParcel;
import com.pushcoin.lib.core.data.PcosAmount;
import com.pushcoin.lib.core.data.Preferences;
import com.pushcoin.lib.core.exceptions.MATUnavailableException;
import com.pushcoin.lib.core.net.PcosServer;
import com.pushcoin.lib.core.security.KeyStore;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.pcos.BlockWriter;
import com.pushcoin.lib.pcos.DocumentWriter;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputBlock;
import com.pushcoin.lib.pcos.OutputDocument;
import com.pushcoin.lib.pcos.PcosError;
import com.pushcoin.srv.gateway.activities.ChargeActivity;
import com.pushcoin.srv.gateway.activities.ChargeActivity.PaymentResponseListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

public class PushCoinService extends Service {
	private static Logger log = Logger.getLogger(PushCoinService.class);

	private Messenger messenger;
	private int startId;

	public PushCoinService() {
		log.d("constructor");
	}

	@Override
	public void onCreate() {
		log.d("onCreate");
		super.onCreate();
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
		if (intent.getAction() == Actions.ACTION_QUERY) {
			Bundle bundle = intent.getExtras();
			query(bundle);
		} else if (intent.getAction() == Actions.ACTION_POLL) {
			Bundle bundle = intent.getExtras();
			poll(bundle);
		} else if (intent.getAction() == Actions.ACTION_CHARGE) {
			Bundle bundle = intent.getExtras();
			charge(bundle);
		} else if (intent.getAction() == Actions.ACTION_IDLE) {
			Bundle bundle = intent.getExtras();
			idle(bundle);
		} else {
			log.d("not supported action: " + intent.getAction());
		}
	}

	private void query(Bundle bundle) {
		log.d("query");

		if (bundle == null) {
			log.e("bundle null");
		} else {
			try {
				this.messenger = (Messenger) bundle.get(KEY_MESSENGER);
			} catch (Exception ex) {
				log.e("start", ex);
			}
		}
	}

	private void poll(Bundle bundle) {
		log.d("poll");
	}

	private void charge(Bundle bundle) {
		log.d("charge");
	}
	
	private void idle(Bundle bundle) {
		log.d("idle");
		cancelCharge();
	}
	
	private void cancelCharge() {
		stop();

		Intent returnIntent = new Intent();
		Result result = new Result();
		result.reason = "User Cancelled";
		result.type = Result.TYPE_CHARGE;
		result.result = Result.RESULT_CANCELED;
		returnIntent.putExtra(Keys.KEY_RESULT, result);
		setResult(Result.RESULT_CANCELED, returnIntent);
		finish();
	}

	private void startIntent(Intent intent) {

		Result result = new Result();
		result.type = Result.TYPE_CHARGE;
		if (intent.getAction().endsWith(Actions.ACTION_CHARGE)) {

			if (!intent.getExtras().containsKey(Keys.KEY_PARAMS)) {
				result.reason = "No Params defined";
				result.result = Result.RESULT_ERROR;
			} else {
				ChargeParams params = (ChargeParams) intent
						.getParcelableExtra(Keys.KEY_PARAMS);

				double amount = params.payment.value
						* Math.pow(10, params.payment.scale);
				if (amount > 0) {
					log.d("Charging " + params.payment.value + " * 10^"
							+ params.payment.scale);
					chargingMessageView.setText("Charging "
							+ params.payment.value + " * 10^"
							+ params.payment.scale);

					display(new DisplayParcel(
							new String[] {
									">>> Tap Now <<<",
									"Amount "
											+ new DecimalFormat("$###0.00")
													.format(amount) },
							DisplayParcel.TextAlignment.CENTER));

					start(amount);
					return;
				} else {
					result.reason = "Payment too low";
					result.result = Result.RESULT_ERROR;
				}
			}
		} else {
			result.reason = "Invalid Action";
			result.result = Result.RESULT_ERROR;
		}

		display(new DisplayParcel(result.reason));

		Intent returnIntent = new Intent();
		returnIntent.putExtra(Keys.KEY_RESULT, result);
		setResult(Result.RESULT_ERROR, returnIntent);

		finish();
	}

	// Received device blob
	private void onPaymentReady(byte[] pta) {
		display(new DisplayParcel("Submitting..."));
		log.i("submitting payment");
		
		Intent intent = getIntent();
		if (intent.getAction().endsWith(Actions.ACTION_CHARGE)) {

			try {
				PcosServer server = new PcosServer();
				if (Preferences.isDemoMode(this, false)) {
					OutputDocument doc = createSuccessResult();
					server.stageAsync(doc, new PaymentResponseListener(server));
				} else {
					String url = PcosServer.getDefaultUrl();
					OutputDocument doc = createPaymentRequest(
							intent.getParcelableExtra(Keys.KEY_PARAMS), pta);
					server.postAsync(url, doc, new PaymentResponseListener(
							server));
				}

			} catch (Exception ex) {
				log.e("exception when submitting payment", ex);
				onPaymentError(ex.getMessage());
			}
		} else {
			log.e("charge intent unexpected lost");
			onPaymentError("Internal Error");
		}
	}

	private void onPaymentError(String reason) {
		Toast.makeText(PushCoinService.this, reason, Toast.LENGTH_LONG).show();
		display(new DisplayParcel(reason));

		Intent returnIntent = new Intent();
		Result result = new Result();
		result.reason = reason;
		result.type = Result.TYPE_CHARGE;
		result.result = Result.RESULT_ERROR;
		returnIntent.putExtra(Keys.KEY_RESULT, result);
		PushCoinService.this.setResult(Result.RESULT_ERROR, returnIntent);
		finish();
	}

	private void onPaymentSuccess(byte[] refData, String trxId,
			boolean isAmountExact, PcosAmount balance, Date utc) {
		Toast.makeText(PushCoinService.this, "Thank You", Toast.LENGTH_LONG)
				.show();
		display(new DisplayParcel("Thank You"));

		// TODO: return information
		Intent returnIntent = new Intent();
		Result result = new Result();
		result.type = Result.TYPE_CHARGE;
		returnIntent.putExtra(Keys.KEY_RESULT, result);

		PushCoinService.this.setResult(Result.RESULT_OK, returnIntent);
		finish();
	}

	private OutputDocument createSuccessResult() throws PcosError,
			MATUnavailableException {
		OutputDocument doc;
		doc = new DocumentWriter("PaymentAck");
		OutputBlock bo = new BlockWriter("Bo");

		// ref data
		bo.writeByteStr(new byte[] { 1, 2, 3, 4 });

		// transaction id
		bo.writeString("trx-demo");

		// is amount exact
		bo.writeBool(true);

		// balance
		PcosAmount amt = new PcosAmount(100, 10);
		amt.write(bo);

		// utc balance time
		bo.writeUlong(new Date().getTime() / 1000);

		doc.addBlock(bo);
		return doc;
	}

	private OutputDocument createPaymentRequest(Parcelable p, byte[] pta)
			throws PcosError, MATUnavailableException {

		KeyStore keyStore = KeyStore.getInstance(this);
		if (keyStore == null || !keyStore.hasMAT())
			throw new MATUnavailableException();

		ChargeParams params = (ChargeParams) p;

		OutputDocument doc;
		doc = new DocumentWriter("PaymentReq");

		OutputBlock r1 = new BlockWriter("R1");

		// MAT
		r1.writeByteStr(keyStore.getMAT());

		// Ref Data
		r1.writeString(params.refData);

		// Creation Time
		r1.writeUlong(new Date().getTime());

		// Total
		new PcosAmount(params.payment.value, params.payment.scale).write(r1);

		log.i("submitting: " + params.payment.value + " "
				+ params.payment.scale);

		// Tax (optional)
		if (params.tax != null) {

			new PcosAmount(params.tax.value, params.tax.scale,
					PcosAmount.OPTIONAL).write(r1);

		} else {
			r1.writeBool(false);
		}

		// Tips (optional)
		if (params.tips != null) {

			new PcosAmount(params.tips.value, params.tips.scale,
					PcosAmount.OPTIONAL).write(r1);

		} else {
			r1.writeBool(false);
		}

		// PassCode
		r1.writeString(params.passcode);

		// Currency
		r1.writeString(params.currency);

		// Invoice
		r1.writeString(params.invoice);

		// Note
		r1.writeString(params.note);

		// GPS (optional)
		if (params.geoLocation != null) {

			r1.writeBool(true);
			r1.writeDouble(params.geoLocation.latitude);
			r1.writeDouble(params.geoLocation.longitude);

		} else {
			r1.writeBool(false);
		}

		// Item Info (not supported)
		r1.writeUint(0);

		// write PTR block
		doc.addBlock(r1);

		// add either PaymentTransactionAuthorization or Payment Transaction
		// Key block
		// which are created by NFC lower lever payload as opaque data
		OutputBlock py = new BlockWriter("Py");
		py.writeBytes(pta);
		doc.addBlock(py);

		return doc;

	}

	public class PaymentResponseListener extends
			PcosServer.PcosResponseListener {
		PaymentResponseListener(PcosServer server) {
			server.super();
		}

		@Override
		public void onErrorResponse(Object tag, byte[] trxId, long ec,
				String reason) {
			log.e("server returned error: " + reason);
			onPaymentError(reason);
		}

		@Override
		public void onResponse(Object tag, InputDocument doc) {
			KeyStore keyStore = KeyStore.getInstance(PushCoinService.this);
			if ((keyStore != null && keyStore.hasMAT()) || Preferences.isDemoMode(PushCoinService.this, false)) {
				try {
					if (doc.getDocumentName().contains("PaymentAck")) {

						InputBlock bo = doc.getBlock("Bo");

						byte[] refData = bo.readByteStr(0);
						String trxId = bo.readString(0);
						boolean isAmountExact = bo.readBool();
						PcosAmount balance = new PcosAmount(bo);
						Date utc = new Date(bo.readUlong());

						onPaymentSuccess(refData, trxId, isAmountExact,
								balance, utc);
						return;
					} else {
						log.e("unexpected message received: "
								+ doc.getDocumentName());
						onPaymentError("Internal Error");
						return;
					}
				} catch (Exception ex) {
					onPaymentError(ex.getMessage());
					return;
				}
			}
			onPaymentError("PushCoin not ready");
		}

		@Override
		public void onError(Object tag, Exception ex) {
			log.e("payment req exception", ex);
			onPaymentError("Error: " + ex.getMessage());
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			case PaymentService.MSGID_COMPLETE:
				onPaymentReady((byte[]) message.obj);
				break;
			case PaymentService.MSGID_ERROR:
				onPaymentError(((Exception) message.obj).getMessage());
			}
		};
	};

	private void start(double amount) {
		Intent intent = new Intent(this, PaymentService.class);
		Messenger messenger = new Messenger(handler);

		intent.setAction(PaymentService.ACTION_START);

		Bundle bundle = new Bundle();
		bundle.putString(PaymentService.KEY_AMOUNT, Double.toString(amount));
		bundle.putParcelable(PaymentService.KEY_MESSENGER, messenger);

		intent.putExtras(bundle);
		startService(intent);
	}

	private void stop() {
		Intent intent = new Intent(this, PaymentService.class);
		intent.setAction(PaymentService.ACTION_STOP);

		startService(intent);
	}

	private void display(DisplayParcel parcel) {
		Intent intent = new Intent(this, PaymentService.class);
		intent.setAction(PaymentService.ACTION_DISPLAY);

		Bundle bundle = new Bundle();
		bundle.putSerializable(PaymentService.KEY_DISPLAYPARCEL, parcel);

		intent.putExtras(bundle);
		startService(intent);
	}

}
