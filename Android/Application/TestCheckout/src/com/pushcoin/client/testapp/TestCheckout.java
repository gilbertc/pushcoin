package com.pushcoin.client.testapp;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pushcoin.lib.integrator.IntentIntegrator;
import com.pushcoin.ifce.connect.data.Amount;
import com.pushcoin.ifce.connect.data.Cancelled;
import com.pushcoin.ifce.connect.data.ChargeParams;
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
	private Button btnPoll;
	private Button btnIdle;
	private Button btnCharge;
	private Button btnQuery;
	private Button btnClear;
	private TextView tvLog;
	private int nextClientId = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.integrator = new IntentIntegrator(this);

		this.btnPoll = (Button) this.findViewById(R.id.btnPoll);
		this.btnIdle = (Button) this.findViewById(R.id.btnIdle);
		this.btnCharge = (Button) this.findViewById(R.id.btnCharge);
		this.btnQuery = (Button) this.findViewById(R.id.btnQuery);
		this.btnClear = (Button) this.findViewById(R.id.btnClear);
		this.tvLog = (TextView) this.findViewById(R.id.tvLog);
		this.tvLog.setMovementMethod(new ScrollingMovementMethod());

		this.btnPoll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Amount amount = new Amount(29, -1);

				PollParams params = new PollParams();
				params.setClientRequestId(Integer.toString((++nextClientId)));
				params.setPayment(amount);

				log("Polling Requested: " + nextClientId);
				integrator.poll(params, TestCheckout.this);
			}
		});
		this.btnCharge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Amount amount = new Amount(29, -1);

				ChargeParams params = new ChargeParams();
				params.setClientRequestId(Integer.toString((++nextClientId)));
				params.setPayment(amount);

				log("Charge Requested: " + nextClientId);
				integrator.charge(params, TestCheckout.this);
			}
		});
		this.btnIdle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				log("Idle Requested");
				integrator.idle();
			}
		});
		this.btnQuery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				QueryParams params = new QueryParams();
				params.setClientRequestId(Integer.toString((++nextClientId)));
				params.setQuery("Test");

				log("Query Requested: " + nextClientId);
				integrator.query(params, TestCheckout.this);
			}
		});
		this.btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tvLog.setText("");
			}
		});

	}

	private void log(String text) {
		tvLog.append("" + new Date().getTime() + ": " + text + "\n");
		final int scrollAmount = tvLog.getLayout().getLineTop(
				tvLog.getLineCount())
				- tvLog.getHeight();
		if (scrollAmount > 0)
			tvLog.scrollTo(0, scrollAmount);
		else
			tvLog.scrollTo(0, 0);
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
			integrator.settings(this);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		log.i("Bootstrapping");
		integrator.bootstrap(this);
	}

	@Override
	public void onResult(QueryResult result) {
		ArrayList<Customer> customers = result.getCustomers();
		log("[" + result.getClientRequestId() + "] Query Results: "
				+ customers.size());
	}

	@Override
	public void onResult(Error err) {
		log("[" + err.getClientRequestId() + "] Error: " + err.getReason());
	}

	@Override
	public void onResult(ChargeResult result) {
		log("[" + result.getClientRequestId() + "] Charge Result Received: "
				+ result.getTrxId());
	}

	@Override
	public void onResult(Cancelled cancelled) {
		log("[" + cancelled.getClientRequestId() + "] Cancelled");
	}
}
