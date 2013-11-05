package com.pushcoin.app.main.activities;

import java.net.UnknownHostException;
import java.util.Date;

import com.pushcoin.app.main.R;
import com.pushcoin.app.main.services.PaymentService;
import com.pushcoin.core.data.DisplayParcel;
import com.pushcoin.core.data.PcosAmount;
import com.pushcoin.core.exceptions.MATUnavailableException;
import com.pushcoin.core.interfaces.Actions;
import com.pushcoin.core.interfaces.Keys;
import com.pushcoin.core.interfaces.Results;
import com.pushcoin.core.net.PcosServer;
import com.pushcoin.core.security.KeyStore;
import com.pushcoin.core.utils.Logger;
import com.pushcoin.pcos.BlockWriter;
import com.pushcoin.pcos.DocumentWriter;
import com.pushcoin.pcos.InputDocument;
import com.pushcoin.pcos.OutputBlock;
import com.pushcoin.pcos.OutputDocument;
import com.pushcoin.pcos.PcosError;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ChargeActivity extends Activity {
	private static Logger log = Logger.getLogger(ChargeActivity.class);

	// UI references.
	private View chargeFormView;
	private View chargeStatusView;
	private TextView chargingMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charge);
		log.i("creating charge activity");

		chargeFormView = findViewById(R.id.charging_form);
		chargeStatusView = findViewById(R.id.charging_status);
		chargingMessageView = (TextView) findViewById(R.id.charging);

		findViewById(R.id.cancel_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						cancelCharge();
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		startIntent(getIntent());
	}

	@Override
	public void onStop() {
		stop();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.charge, menu);
		return true;
	}

	private void cancelCharge() {
		stop();
		setResult(Results.CANCELED.getValue());
		finish();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			chargeStatusView.setVisibility(View.VISIBLE);
			chargeStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							chargeStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			chargeFormView.setVisibility(View.VISIBLE);
			chargeFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							chargeFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			chargeStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			chargeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private void startIntent(Intent intent) {
		if (intent.getAction().endsWith(Actions.ACTION_CHARGE)) {
			long value = intent.getLongExtra(Keys.KEY_PAYMENT_VALUE, 0);
			int scale = intent.getIntExtra(Keys.KEY_PAYMENT_SCALE, 0);

			double amount = value * Math.pow(10, scale);
			if (amount > 0) {
				log.d("Charging " + value + " * 10^" + scale);
				chargingMessageView.setText("Charging " + value + " * 10^"
						+ scale);
				start(amount);
				return;
			}
		}
		setResult(Results.ERROR.getValue());
		finish();
	}

	// Received device blob
	private void onPaymentReady(byte[] pta) {
		display(new DisplayParcel("Submitting..."));
		log.i("submitting payment");

		Intent intent = getIntent();
		if (intent.getAction().endsWith(Actions.ACTION_CHARGE)) {

			try {
				String url = PcosServer.getDefaultUrl();
				if (url.isEmpty())
					throw new UnknownHostException();

				OutputDocument doc = createPaymentRequest(intent.getExtras(),
						pta);

				PcosServer server = new PcosServer();
				server.postAsync(url, doc, new PaymentResponseListener(server));

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
		Toast.makeText(ChargeActivity.this, reason, Toast.LENGTH_LONG).show();
		display(new DisplayParcel(reason));

		ChargeActivity.this.setResult(Results.ERROR.getValue());
		finish();
	}

	private void onPaymentSuccess() {
		Toast.makeText(ChargeActivity.this, "Thank You", Toast.LENGTH_LONG)
				.show();
		display(new DisplayParcel("Thank You"));

		ChargeActivity.this.setResult(Results.OK.getValue());
		finish();
	}

	private OutputDocument createPaymentRequest(Bundle bundle, byte[] pta)
			throws PcosError, MATUnavailableException {

		KeyStore keyStore = KeyStore.getInstance();
		if (keyStore == null || !keyStore.hasMAT())
			throw new MATUnavailableException();

		OutputDocument doc;
		doc = new DocumentWriter("PaymentReq");

		OutputBlock r1 = new BlockWriter("R1");

		// MAT
		r1.writeByteStr(keyStore.getMAT());

		// Ref Data
		r1.writeString(bundle.getString(Keys.KEY_REF_DATA, ""));

		// Creation Time
		r1.writeUlong(new Date().getTime());

		// Total
		new PcosAmount(bundle.getLong(Keys.KEY_PAYMENT_VALUE, 0),
				bundle.getInt(Keys.KEY_PAYMENT_SCALE, 0)).write(r1);

		log.i("submitting: " + bundle.getLong(Keys.KEY_PAYMENT_VALUE, 0) + " "
				+ bundle.getInt(Keys.KEY_PAYMENT_SCALE, 0));

		// Tax (optional)
		if (bundle.containsKey(Keys.KEY_TAX_VALUE)
				&& bundle.containsKey(Keys.KEY_TAX_SCALE)) {

			new PcosAmount(bundle.getLong(Keys.KEY_TAX_VALUE, 0),
					bundle.getInt(Keys.KEY_TAX_SCALE, 0), PcosAmount.OPTIONAL)
					.write(r1);

		} else {
			r1.writeBool(false);
		}

		// Tips (optional)
		if (bundle.containsKey(Keys.KEY_TIPS_VALUE)
				&& bundle.containsKey(Keys.KEY_TIPS_SCALE)) {

			new PcosAmount(bundle.getLong(Keys.KEY_TIPS_VALUE, 0),
					bundle.getInt(Keys.KEY_TIPS_SCALE, 0), PcosAmount.OPTIONAL)
					.write(r1);

		} else {
			r1.writeBool(false);
		}

		// Passcode
		r1.writeString(bundle.getString(Keys.KEY_PASSCODE, ""));

		// Currency
		r1.writeString(bundle.getString(Keys.KEY_CURRENCY, "USD"));

		// Invoice
		r1.writeString(bundle.getString(Keys.KEY_INVOICE, ""));

		// Note
		r1.writeString(bundle.getString(Keys.KEY_NOTE, ""));

		// GPS (optional)
		if (bundle.containsKey(Keys.KEY_GEOLOCATION_LAT)
				&& bundle.containsKey(Keys.KEY_GEOLOCATION_LONG)) {

			r1.writeBool(true);
			r1.writeDouble(bundle.getDouble(Keys.KEY_GEOLOCATION_LAT));
			r1.writeDouble(bundle.getDouble(Keys.KEY_GEOLOCATION_LONG));

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
			KeyStore keyStore = KeyStore.getInstance();
			if (keyStore != null) {
				try {
					if (doc.getDocumentName().contains("PaymentAck")) {
						onPaymentSuccess();
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
			onPaymentError(ex.getMessage());
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			case PaymentService.MSGID_COMPLETE:
				onPaymentReady((byte[]) message.obj);
				break;
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
