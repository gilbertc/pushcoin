package com.pushcoin.client.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pushcoin.lib.integrator.IntentIntegrator;
import com.pushcoin.ifce.connect.Keys;
import com.pushcoin.ifce.connect.data.ChargeParams;
import com.pushcoin.ifce.connect.data.Result;

import com.pushcoin.lib.core.utils.Logger;

public class TestCheckout extends Activity {
	private static Logger log = Logger.getLogger(TestCheckout.class);

	private IntentIntegrator integrator;
	private Button btnStart;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.integrator = new IntentIntegrator(this);

		this.btnStart = (Button) this.findViewById(R.id.btnStart);

		this.btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChargeParams params = new ChargeParams();
				params.payment.value = 29;
				params.payment.scale = -1;

				integrator.charge(params);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			integrator.settings();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		log.i("Bootstrapping");
		integrator.bootstrap();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		log.i("Result = " + resultCode);

		if (intent != null && intent.getExtras().containsKey(Keys.KEY_RESULT)) {
			Result res = (Result) intent.getParcelableExtra(Keys.KEY_RESULT);
			switch (res.type) {
			case Result.TYPE_BOOTSTRAP:
				log.i("Bootstrap Result: " + res.result + " (" + res.reason
						+ ")");
				break;
			case Result.TYPE_CHARGE:
				log.i("Charge Result: " + res.result + " (" + res.reason + ")");
				break;
			case Result.TYPE_REGISTER:
				log.i("Register Result: " + res.result + " (" + res.reason
						+ ")");
				break;
			}
		}
	}

}
