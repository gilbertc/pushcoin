package com.pushcoin.app.main.activities;

import com.pushcoin.app.main.R;
import com.pushcoin.app.main.R.layout;
import com.pushcoin.app.main.R.menu;
import com.pushcoin.app.main.activities.RegisterDeviceActivity.RegistrationResponseListener;
import com.pushcoin.core.data.DisplayParcel;
import com.pushcoin.core.interfaces.Actions;
import com.pushcoin.core.interfaces.Keys;
import com.pushcoin.core.interfaces.Results;
import com.pushcoin.core.net.PcosServer;
import com.pushcoin.core.services.PushCoinService;
import com.pushcoin.core.utils.Logger;
import com.pushcoin.pcos.DocumentWriter;
import com.pushcoin.pcos.InputDocument;

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
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
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
			int value = intent.getIntExtra(Keys.KEY_PAYMENT_VALUE, 0);
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

	private void start(double amount) {
		Intent intent = new Intent(this, PushCoinService.class);
		Messenger messenger = new Messenger(handler);

		intent.setAction(PushCoinService.ACTION_START);

		Bundle bundle = new Bundle();
		bundle.putString(PushCoinService.KEY_AMOUNT, Double.toString(amount));
		bundle.putParcelable(PushCoinService.KEY_MESSENGER, messenger);

		intent.putExtras(bundle);
		startService(intent);
	}

	private void stop() {
		Intent intent = new Intent(this, PushCoinService.class);
		intent.setAction(PushCoinService.ACTION_STOP);

		startService(intent);
	}

	private void display(DisplayParcel parcel) {
		Intent intent = new Intent(this, PushCoinService.class);
		intent.setAction(PushCoinService.ACTION_DISPLAY);

		Bundle bundle = new Bundle();
		bundle.putSerializable(PushCoinService.KEY_DISPLAYPARCEL, parcel);

		intent.putExtras(bundle);
		startService(intent);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
			case PushCoinService.MSGID_COMPLETE:
				Toast.makeText(ChargeActivity.this, "Completed",
						Toast.LENGTH_LONG).show();
				display(new DisplayParcel("Completed"));
				ChargeActivity.this.setResult(Results.OK.getValue());
				finish();
				break;
			}
		};
	};

}
