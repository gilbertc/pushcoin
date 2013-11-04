package com.pushcoin.app.main.activities;

import java.io.IOException;
import java.net.UnknownHostException;

import com.pushcoin.app.main.R;
import com.pushcoin.core.net.PcosServer;
import com.pushcoin.core.security.KeyStore;
import com.pushcoin.core.utils.Logger;
import com.pushcoin.pcos.BlockWriter;
import com.pushcoin.pcos.DocumentWriter;
import com.pushcoin.pcos.InputBlock;
import com.pushcoin.pcos.InputDocument;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterDeviceActivity extends Activity {
	private static Logger log = Logger.getLogger(RegisterDeviceActivity.class);

	private PcosServer server = null;

	private String registrationCode;

	// UI references.
	private EditText registrationCodeView;
	private View registrationFormView;
	private View registrationStatusView;
	private TextView registrationStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register_device);

		registrationCode = "";
		registrationCodeView = (EditText) findViewById(R.id.registration_code);
		registrationCodeView.setText(registrationCode);

		registrationFormView = findViewById(R.id.registration_form);
		registrationStatusView = findViewById(R.id.registration_status);
		registrationStatusMessageView = (TextView) findViewById(R.id.registration_status_message);

		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegistration();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.register_device, menu);
		return true;
	}

	public void attemptRegistration() {
		if (server != null) {
			return;
		}

		KeyStore keyStore = KeyStore.getInstance(this);
		keyStore.createNewKeys(this);

		// Reset errors.
		registrationCodeView.setError(null);

		// Store values at the time of the registration attempt.
		registrationCode = registrationCodeView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid registrationCode.
		if (TextUtils.isEmpty(registrationCode)) {
			registrationCodeView
					.setError(getString(R.string.error_field_required));
			focusView = registrationCodeView;
			cancel = true;
		} else if (registrationCode.length() < 4) {
			registrationCodeView
					.setError(getString(R.string.error_invalid_registration_code));
			focusView = registrationCodeView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task
			registrationStatusMessageView
					.setText(R.string.registration_progress_registering);
			showProgress(true);

			try {
				DocumentWriter writer = new DocumentWriter("Register");
				BlockWriter bo = new BlockWriter("Bo");
				bo.writeString(registrationCode);
				bo.writeByteStr(keyStore.getPublicKey().getEncoded());
				writer.addBlock(bo);

				String url = PcosServer.getDefaultUrl(this);
				if (url.isEmpty())
					throw new UnknownHostException();

				server = new PcosServer();
				server.postAsync(url, writer, new RegistrationResponseListener(
						server));
			} catch (Exception ex) {
				registrationCodeView.setError(ex.getMessage());
				registrationCodeView.requestFocus();
				showProgress(false);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			registrationStatusView.setVisibility(View.VISIBLE);
			registrationStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							registrationStatusView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			registrationFormView.setVisibility(View.VISIBLE);
			registrationFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							registrationFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			registrationStatusView.setVisibility(show ? View.VISIBLE
					: View.GONE);
			registrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public class RegistrationResponseListener extends
			PcosServer.PcosResponseListener {
		RegistrationResponseListener(PcosServer server) {
			server.super();
		}

		@Override
		public void onErrorResponse(Object tag, byte[] trxId, long ec,
				String reason) {
			log.e("server returned error: " + reason);
			registrationCodeView.setError(reason);
			registrationCodeView.requestFocus();
		}

		@Override
		public void onResponse(Object tag, InputDocument doc) {
			boolean ok = false;
			KeyStore keyStore = KeyStore.getInstance();
			if (keyStore != null) {
				try {
					if (doc.getDocumentName().contains("RegisterAck")) {
						InputBlock bo = doc.getBlock("Bo");
						if (bo != null) {
							keyStore.setMAT(RegisterDeviceActivity.this,
									bo.readByteStr(0));
							ok = true;
						}
					} else {
						log.e("unexpected message received: "
								+ doc.getDocumentName());
					}
				} catch (Exception ex) {
					log.e("exception on register ack");
					registrationCodeView.setError(ex.getMessage());
					registrationCodeView.requestFocus();
					ok = false;
				}
			}

			if (ok)
				finish();
			else if (keyStore != null)
				keyStore.reset(RegisterDeviceActivity.this);
		}

		@Override
		public void onError(Object tag, Exception ex) {

			KeyStore keyStore = KeyStore.getInstance();
			if (keyStore != null)
				keyStore.reset(RegisterDeviceActivity.this);

			log.e("register ack exception", ex);

			registrationCodeView.setError(ex.getMessage());
			registrationCodeView.requestFocus();
		}

		@Override
		public void onFinished(Object tag) {
			showProgress(false);
			server = null;
		}

	}

}
