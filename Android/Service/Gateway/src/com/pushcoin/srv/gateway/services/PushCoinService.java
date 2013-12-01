package com.pushcoin.srv.gateway.services;

import java.text.DecimalFormat;
import java.util.Date;

import com.pushcoin.ifce.connect.Actions;
import com.pushcoin.ifce.connect.Messages;
import com.pushcoin.ifce.connect.data.Amount;
import com.pushcoin.ifce.connect.data.CallbackParams;
import com.pushcoin.ifce.connect.data.Cancelled;
import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.ChargeResult;
import com.pushcoin.ifce.connect.data.PollParams;
import com.pushcoin.ifce.connect.data.QueryParams;
import com.pushcoin.ifce.connect.data.Error;
import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.lib.core.data.DisplayParcel;
import com.pushcoin.lib.core.data.DisplayParcel.TextAlignment;
import com.pushcoin.lib.core.data.PcosAmount;
import com.pushcoin.lib.core.data.Preferences;
import com.pushcoin.lib.core.exceptions.MATUnavailableException;
import com.pushcoin.lib.core.net.PcosServer;
import com.pushcoin.lib.core.query.IQuery;
import com.pushcoin.lib.core.security.KeyStore;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.pcos.BlockWriter;
import com.pushcoin.lib.pcos.DocumentWriter;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputBlock;
import com.pushcoin.lib.pcos.OutputDocument;
import com.pushcoin.lib.pcos.PcosError;
import com.pushcoin.srv.gateway.demo.QueryResultBuilder;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class PushCoinService extends Service {
	private static Logger log = Logger.getLogger(PushCoinService.class);

	private PollParams pollParams = null;

	public PushCoinService() {
		log.d("constructor");
	}

	@Override
	public void onCreate() {
		log.d("onCreate");
		super.onCreate();
		idle();
	}

	@Override
	public void onStart(Intent intent, int startId) {
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
		if (intent.getAction().endsWith(Actions.ACTION_QUERY)) {
			query(new QueryParams(intent.getExtras()));
		} else if (intent.getAction().endsWith(Actions.ACTION_POLL)) {
			poll(new PollParams(intent.getExtras()));
		} else if (intent.getAction().endsWith(Actions.ACTION_CHARGE)) {
			charge(new ChargeParams(intent.getExtras()));
		} else if (intent.getAction().endsWith(Actions.ACTION_IDLE)) {
			idle();
		} else {
			log.d("not supported action: " + intent.getAction());
		}
	}

	private void poll(PollParams params) {
		log.d("poll");

		Amount payment = params.getPayment();
		if (payment == null) {
			onError(params, "Invalid Poll Params");
			return;
		}

		double amount = payment.value * Math.pow(10, payment.scale);
		if (amount <= 0) {
			onError(params, "Payment too low");
			return;
		}

		onNewPollRequest(params);

		log.d("Charging " + payment.value + " * 10^" + payment.scale);
		display(new DisplayParcel(new String[] { ">>> Tap Now <<<",
				"Amount " + new DecimalFormat("$###0.00").format(amount) },
				DisplayParcel.TextAlignment.CENTER));

		start(amount);
	}

	private void query(QueryParams params) {
		log.d("query");

		try {
			PcosServer server = new PcosServer();
			if (Preferences.isDemoMode(this, false)) {
				OutputDocument doc = createQuerySuccessResult();
				server.stageAsync(params, doc,
						new QueryResponseListener(server));
			} else {
				onError(params, "Query not supported in Production");
			}

		} catch (Exception ex) {
			log.e("exception when submitting payment", ex);
			onError(params, ex.getMessage());
		}
	}

	private void charge(ChargeParams params) {
		log.d("charge");

		try {
			PcosServer server = new PcosServer();
			if (Preferences.isDemoMode(this, false)) {
				OutputDocument doc = createPaymentSuccessResult();
				server.stageAsync(params, doc, new PaymentResponseListener(
						server, false));
			} else {
				onError(params, "Charge not supported in Production");
			}

		} catch (Exception ex) {
			log.e("exception when submitting payment", ex);
			onError(params, ex.getMessage());
		}
	}

	private void idle() {
		log.d("idle");

		if (this.pollParams != null)
			onCancelled(this.pollParams);

		this.pollParams = null;
		display(new DisplayParcel("PUSHCOIN", TextAlignment.CENTER));
		stop();
	}

	private void onPollError(String reason) {
		PollParams params = this.pollParams;
		onPollCompleted();
		onError(params, reason);
	}

	private void onError(CallbackParams params, String reason) {

		if (params != null) {
			Error error = new Error();
			error.setReason(reason);
			error.setClientRequestId(params.getClientRequestId());

			Messenger messenger = params.getMessenger();
			if (messenger != null) {
				Message m = Message.obtain();
				m.what = Messages.MSGID_ERROR;
				m.setData(error.getBundle());

				try {
					messenger.send(m);
				} catch (Exception ex) {
					log.e("messenger", ex);
				}
			}
		}

		log.e(reason);
		display(new DisplayParcel(reason));
	}

	private void onCancelled(CallbackParams params) {
		if (params != null) {
			log.e("cancelled");
			Cancelled cancelled = new Cancelled();
			cancelled.setClientRequestId(params.getClientRequestId());

			Messenger messenger = params.getMessenger();
			if (messenger != null) {
				Message m = Message.obtain();
				m.what = Messages.MSGID_CANCELLED;
				m.setData(cancelled.getBundle());

				try {
					messenger.send(m);
				} catch (Exception ex) {
					log.e("messenger", ex);
				}
			}
		}
	}

	// Received a query from device
	private void onQueryReady(IQuery query)
	{
		display(new DisplayParcel("Querying"));
		log.i("querying");
		
		if (this.pollParams == null) {
			onPollError("Poll params lost");
			return;
		}
		
		// Create query params from the iquery result and our poll params
		QueryParams params = new QueryParams();
		params.setMessenger(this.pollParams.getMessenger());
		params.setClientRequestId(this.pollParams.getClientRequestId());
		params.setQuery("--Fingerprint--");
		
		query(params);
	}
	
	// Received device blob
	private void onPaymentReady(byte[] pta) {
		display(new DisplayParcel("Submitting..."));
		log.i("submitting payment");

		if (this.pollParams == null) {
			onPollError("Poll params lost");
			return;
		}
		PollParams params = this.pollParams;

		// From here, we have a definite params obj.
		onPollCompleted();

		try {
			PcosServer server = new PcosServer();
			if (Preferences.isDemoMode(this, false)) {
				OutputDocument doc = createPaymentSuccessResult();
				server.stageAsync(params, doc, new PaymentResponseListener(
						server, true));
			} else {
				String url = PcosServer.getDefaultUrl();
				OutputDocument doc = createPaymentRequest(params, pta);
				server.postAsync(params, url, doc, new PaymentResponseListener(
						server, true));
			}

		} catch (Exception ex) {
			log.e("exception when submitting payment", ex);
			onPaymentError(params, ex.getMessage());
		}
	}

	private void onQueryError(QueryParams params, String reason) {
		onError(params, reason);
	}

	private void onQuerySuccess(QueryParams params) {
		if (!Preferences.isDemoMode(this, false)) {
			log.e("Query not supported in production");
			return;
		}

		if (params == null) {
			log.e("Query params lost");
			return;
		}

		Messenger messenger = params.getMessenger();
		if (messenger == null) {
			log.e("invalid query params");
			return;
		}

		QueryResult res = QueryResultBuilder
				.makeResult(this, params.getQuery());
		res.setClientRequestId(params.getClientRequestId());
		log.d("Query received customers: " + res.getCustomers().size());

		Message m = Message.obtain();
		m.what = Messages.MSGID_QUERY_RESULT;
		m.setData(res.getBundle());

		try {
			messenger.send(m);
		} catch (Exception ex) {
			log.e("onQuerySuccess", ex);
		}
	}

	private void onPaymentError(ChargeParams params, String reason) {
		onError(params, reason);
	}

	private void onPaymentSuccess(ChargeParams params, byte[] refData,
			String trxId, boolean isAmountExact, PcosAmount balance, Date utc,
			boolean thankyou) {

		if (thankyou)
			display(new DisplayParcel("Thank You", TextAlignment.CENTER));

		if (params == null) {
			onError(params, "Poll params lost");
			return;
		}

		Messenger messenger = params.getMessenger();
		if (messenger == null) {
			onError(params, "Invalid poll params");
			return;
		}

		ChargeResult res = new ChargeResult();
		res.setClientRequestId(params.getClientRequestId());
		res.setRefData(refData);
		res.setTrxId(trxId);
		res.setIsAmountExact(isAmountExact);
		res.setBalance(new Amount(balance.getValue(), balance.getScale()));
		res.setUtc(utc.getTime());

		Message m = Message.obtain();
		m.what = Messages.MSGID_CHARGE_RESULT;
		m.setData(res.getBundle());

		try {
			messenger.send(m);
		} catch (Exception ex) {
			log.e("onPaymentSuccess", ex);
		}

	}

	private OutputDocument createQuerySuccessResult() throws PcosError {
		OutputDocument doc;
		doc = new DocumentWriter("QueryAck");
		return doc;
	}

	private OutputDocument createPaymentSuccessResult() throws PcosError,
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

	private OutputDocument createPaymentRequest(ChargeParams params, byte[] pta)
			throws PcosError, MATUnavailableException {

		KeyStore keyStore = KeyStore.getInstance(this);
		if (keyStore == null || !keyStore.hasMAT())
			throw new MATUnavailableException();

		OutputDocument doc;
		doc = new DocumentWriter("PaymentReq");

		OutputBlock r1 = new BlockWriter("R1");

		// MAT
		r1.writeByteStr(keyStore.getMAT());

		// Ref Data
		r1.writeString(params.getRefData());

		// Creation Time
		r1.writeUlong(new Date().getTime());

		// Total
		new PcosAmount(params.getPayment().value, params.getPayment().scale)
				.write(r1);

		log.i("submitting: " + params.getPayment().value + " "
				+ params.getPayment().scale);

		// Tax (optional)
		if (params.getTax() != null) {

			new PcosAmount(params.getTax().value, params.getTax().scale,
					PcosAmount.OPTIONAL).write(r1);

		} else {
			r1.writeBool(false);
		}

		// Tips (optional)
		if (params.getTips() != null) {

			new PcosAmount(params.getTips().value, params.getTips().scale,
					PcosAmount.OPTIONAL).write(r1);

		} else {
			r1.writeBool(false);
		}

		// PassCode
		r1.writeString(params.getPasscode());

		// Currency
		r1.writeString(params.getCurrency());

		// Invoice
		r1.writeString(params.getInvoice());

		// Note
		r1.writeString(params.getNote());

		// GPS (optional)
		if (params.getGeoLocation() != null) {

			r1.writeBool(true);
			r1.writeDouble(params.getGeoLocation().latitude);
			r1.writeDouble(params.getGeoLocation().longitude);

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

	public class QueryResponseListener extends PcosServer.PcosResponseListener {
		QueryResponseListener(PcosServer server) {
			server.super();
		}

		@Override
		public void onErrorResponse(Object tag, byte[] trxId, long ec,
				String reason) {
			log.e("server returned error: " + reason);
			onQueryError((QueryParams) tag, reason);
		}

		@Override
		public void onResponse(Object tag, InputDocument doc) {
			onQuerySuccess((QueryParams) tag);
		}

		@Override
		public void onError(Object tag, Exception ex) {
			log.e("query req exception", ex);
			onQueryError((QueryParams) tag, "Error: " + ex.getMessage());
		}
	}

	public class PaymentResponseListener extends
			PcosServer.PcosResponseListener {
		private boolean thankyou = false;

		PaymentResponseListener(PcosServer server, boolean thankyou) {
			server.super();
			this.thankyou = thankyou;
		}

		@Override
		public void onErrorResponse(Object tag, byte[] trxId, long ec,
				String reason) {
			log.e("server returned error: " + reason);
			onPaymentError((PollParams) tag, reason);
		}

		@Override
		public void onResponse(Object tag, InputDocument doc) {
			KeyStore keyStore = KeyStore.getInstance(PushCoinService.this);
			if ((keyStore != null && keyStore.hasMAT())
					|| Preferences.isDemoMode(PushCoinService.this, false)) {
				try {
					if (doc.getDocumentName().contains("PaymentAck")) {

						InputBlock bo = doc.getBlock("Bo");

						byte[] refData = bo.readByteStr(0);
						String trxId = bo.readString(0);
						boolean isAmountExact = bo.readBool();
						PcosAmount balance = new PcosAmount(bo);
						Date utc = new Date(bo.readUlong());

						onPaymentSuccess((ChargeParams) tag, refData, trxId,
								isAmountExact, balance, utc, thankyou);
						return;
					} else {
						log.e("unexpected message received: "
								+ doc.getDocumentName());
						onPaymentError((ChargeParams) tag, "Internal Error");
						return;
					}
				} catch (Exception ex) {
					onPaymentError((ChargeParams) tag, ex.getMessage());
					return;
				}
			}
			onPaymentError((ChargeParams) tag, "PushCoin not ready");
		}

		@Override
		public void onError(Object tag, Exception ex) {
			log.e("payment req exception", ex);
			onPaymentError((ChargeParams) tag, "Error: " + ex.getMessage());
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			case PaymentService.MSGID_COMPLETE:
				onPaymentReady((byte[]) message.obj);
				break;
			case PaymentService.MSGID_QUERY:
				onQueryReady((IQuery) message.obj);
				break;
			case PaymentService.MSGID_ERROR:
				onPollError(((Exception) message.obj).getMessage());
				break;
			}
		};
	};

	private void start(double amount) {
		Intent intent = new Intent(this, PaymentService.class);
		Messenger messenger = new Messenger(handler);

		intent.setAction(PaymentService.ACTION_START);

		Bundle bundle = new Bundle();
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

	private void onNewPollRequest(PollParams params) {
		if (this.pollParams != null)
			onCancelled(this.pollParams);

		this.pollParams = params;
	}

	private void onPollCompleted() {
		this.pollParams = null;
	}
}
