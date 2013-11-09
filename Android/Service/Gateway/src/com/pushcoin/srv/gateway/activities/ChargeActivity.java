package com.pushcoin.srv.gateway.activities;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;

import com.pushcoin.srv.gateway.R;
import com.pushcoin.srv.gateway.services.PaymentService;
import com.pushcoin.lib.core.data.DisplayParcel;
import com.pushcoin.lib.core.data.PcosAmount;
import com.pushcoin.lib.core.exceptions.MATUnavailableException;
import com.pushcoin.lib.core.net.PcosServer;
import com.pushcoin.lib.core.security.KeyStore;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.pcos.BlockWriter;
import com.pushcoin.lib.pcos.DocumentWriter;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputBlock;
import com.pushcoin.lib.pcos.OutputDocument;
import com.pushcoin.lib.pcos.PcosError;

import com.pushcoin.ifce.connect.Actions;
import com.pushcoin.ifce.connect.Keys;
import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.Result;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
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

		Intent returnIntent = new Intent();
		Result result = new Result();
		result.reason = "User Cancelled";
		result.type = Result.TYPE_CHARGE;
		result.result = Result.RESULT_CANCELED;
		returnIntent.putExtra(Keys.KEY_RESULT, result);
		setResult(Result.RESULT_CANCELED, returnIntent);
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
				String url = PcosServer.getDefaultUrl();
				if (url.isEmpty())
					throw new UnknownHostException();

				OutputDocument doc = createPaymentRequest(
						intent.getParcelableExtra(Keys.KEY_PARAMS), pta);

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

		Intent returnIntent = new Intent();
		Result result = new Result();
		result.reason = reason;
		result.type = Result.TYPE_CHARGE;
		result.result = Result.RESULT_ERROR;
		returnIntent.putExtra(Keys.KEY_RESULT, result);
		ChargeActivity.this.setResult(Result.RESULT_ERROR, returnIntent);
		finish();
	}

	private void onPaymentSuccess() {
		Toast.makeText(ChargeActivity.this, "Thank You", Toast.LENGTH_LONG)
				.show();
		display(new DisplayParcel("Thank You"));

		Intent returnIntent = new Intent();
		Result result = new Result();
		result.type = Result.TYPE_CHARGE;
		returnIntent.putExtra(Keys.KEY_RESULT, result);
		ChargeActivity.this.setResult(Result.RESULT_OK, returnIntent);
		finish();
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
			KeyStore keyStore = KeyStore.getInstance(ChargeActivity.this);
			if (keyStore != null && keyStore.hasMAT()) {
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
			onPaymentError("Error: " + ex.getMessage());
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
