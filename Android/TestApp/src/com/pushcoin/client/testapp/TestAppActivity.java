package com.pushcoin.client.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.pushcoin.app.integrator.IntentIntegrator;
import com.pushcoin.core.data.DisplayParcel;
import com.pushcoin.core.services.PushCoinService;

public class TestAppActivity extends Activity {

	private Button btnStart;
	private Button btnStop;
	private Button btnDisplay;
	private EditText etDisplay;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.btnStart = (Button) this.findViewById(R.id.btnStart);
		this.btnStop = (Button) this.findViewById(R.id.btnStop);
		this.btnDisplay = (Button) this.findViewById(R.id.btnDisplay);
		this.etDisplay = (EditText) this.findViewById(R.id.etDisplay);
		

		this.btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				start();
				display(new DisplayParcel("touch now")); 
			}
		});

		this.btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
				display(new DisplayParcel("cancelled"));
			}
		});
		this.btnDisplay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DisplayParcel parcel = new DisplayParcel(etDisplay.getText().toString()); 
				display(parcel);
			}
		});
	}
	
	private void start()
	{
		Intent intent = new Intent(TestAppActivity.this, PushCoinService.class);
		Messenger messenger = new Messenger(handler);

		intent.setAction(PushCoinService.ACTION_START); 

		Bundle bundle = new Bundle();
		bundle.putString(PushCoinService.KEY_AMOUNT, "12");
		bundle.putParcelable(PushCoinService.KEY_MESSENGER, messenger);

		intent.putExtras(bundle);
		startService(intent);
	}

	private void stop()
	{
		Intent intent = new Intent(TestAppActivity.this, PushCoinService.class);
		Messenger messenger = new Messenger(handler);

		intent.setAction(PushCoinService.ACTION_STOP);

		Bundle bundle = new Bundle();
		bundle.putParcelable(PushCoinService.KEY_MESSENGER, messenger);

		intent.putExtras(bundle);
		startService(intent);
	}

	
	private void display(DisplayParcel parcel)
	{
		Intent intent = new Intent(TestAppActivity.this, PushCoinService.class);
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
				Toast.makeText(TestAppActivity.this, "Completed",
						Toast.LENGTH_LONG).show();
				display(new DisplayParcel("Completed"));
				break;

			}
		};
	};

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.bootstrap();
	}

}
