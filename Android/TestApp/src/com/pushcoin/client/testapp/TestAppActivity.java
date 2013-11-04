package com.pushcoin.client.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pushcoin.app.integrator.IntentIntegrator;
import com.pushcoin.core.interfaces.Keys;
import com.pushcoin.core.utils.Logger;

public class TestAppActivity extends Activity {
	private static Logger log = Logger.getLogger(TestAppActivity.class);

	private IntentIntegrator integrator;
	private Button btnStart;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.integrator = new IntentIntegrator(this);

		this.btnStart = (Button) this.findViewById(R.id.btnStart);

		this.btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putLong(Keys.KEY_PAYMENT_VALUE, 29);
				bundle.putInt(Keys.KEY_PAYMENT_SCALE, -1);
				integrator.charge(bundle);
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
		// integrator.bootstrap();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		log.i("Result = " + resultCode);
	}

}
