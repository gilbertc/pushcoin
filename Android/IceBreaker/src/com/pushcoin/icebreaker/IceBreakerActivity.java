package com.pushcoin.icebreaker;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.AsyncTask;
import com.pushcoin.pcos;

public class IceBreakerActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		statusField_ = (TextView) findViewById(R.id.status_field);
		refreshButton_ = (Button) findViewById(R.id.refresh_button);

		refreshButton_.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refreshView();
			}
		});

		// fetch latest account state
		refreshView();
	}

	private void refreshView()
	{
		// Load balance on startup
		new DownloadHistoryTask(refreshButton_, statusField_).execute("mat here");
	}

  protected void onResume() 
	{
		super.onResume();
		refreshView();
	}

	private static class DownloadHistoryTask extends AsyncTask<String, Void, Long>
	{
		DownloadHistoryTask(Button refresh, TextView statusField)
		{
			refreshButton_ = refresh;
			statusField_ = statusField;
		}

		protected void onPreExecute()
		{
			statusField_.setText("Loading...");

			// stop useless repeatition
			refreshButton_.setEnabled(false);
		}

		protected Long doInBackground(String... mat)
		{
			pcos.OutputBlock bo = new pcos.BlockWriter( "Bo" );

			// simulate long fetch
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}

			return 0L;
		}

		protected void onPostExecute(Long result) 
		{
			statusField_.setText("as of Today 5:15 PM");

			// allow manual refresh
			refreshButton_.setEnabled(true);
		}

		Button refreshButton_;
		TextView statusField_;
	}

	TextView statusField_;
	Button refreshButton_;
}
