package com.pushcoin.client.testapp;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.pushcoin.lib.integrator.IntentIntegrator;
import com.pushcoin.ifce.connect.data.Amount;
import com.pushcoin.ifce.connect.data.ChargeResult;
import com.pushcoin.ifce.connect.data.Customer;
import com.pushcoin.ifce.connect.data.Error;
import com.pushcoin.ifce.connect.data.PollParams;
import com.pushcoin.ifce.connect.data.QueryParams;
import com.pushcoin.ifce.connect.data.QueryResult;
import com.pushcoin.ifce.connect.listeners.PollResultListener;

import com.pushcoin.lib.core.utils.Logger;

public class TestCheckout extends Activity implements PollResultListener {
	private static Logger log = Logger.getLogger(TestCheckout.class);

	private IntentIntegrator integrator;
	private Button btnStart;
	private Button btnQuery;

	private OnClickListener startCharge;
	private OnClickListener stopCharge;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.integrator = new IntentIntegrator(this);

		this.btnStart = (Button) this.findViewById(R.id.btnStart);
		this.btnQuery = (Button) this.findViewById(R.id.btnQuery);

		this.startCharge = new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnStart.setText("Stop Charge");
				PollParams params = new PollParams();
				Amount amount = new Amount(29, -1);
				params.setPayment(amount);

				btnStart.setOnClickListener(stopCharge);
				integrator.poll(params, TestCheckout.this);
			}
		};

		this.stopCharge = new OnClickListener() {
			@Override
			public void onClick(View v) {
				completeCharge();
			}
		};

		this.btnStart.setOnClickListener(startCharge);
		this.btnQuery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnQuery.setText("Querying");
				QueryParams params = new QueryParams();
				params.setQuery("Test");
				integrator.query(params, TestCheckout.this);
			}
		});
	}

	private void completeCharge() {
		btnStart.setText("Start Charge");
		btnStart.setOnClickListener(startCharge);
		integrator.idle();
	}

	private void completeQuery() {
		btnQuery.setText("Start Query");
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
	public void onResult(QueryResult result) {
		ArrayList<Customer> customers = result.getCustomers();
		Toast.makeText(this, "Query Results: " + customers.size(),
				Toast.LENGTH_LONG).show();
		completeQuery();
	}

	@Override
	public void onResult(Error err) {
		Toast.makeText(this, "Error: " + err.getReason(), Toast.LENGTH_LONG)
				.show();
		completeCharge();
		completeQuery();
	}

	@Override
	public void onResult(ChargeResult result) {
		Toast.makeText(this, "Charge Result Received: " + result.getTrxId(),
				Toast.LENGTH_LONG).show();
		completeCharge();
	}

}
