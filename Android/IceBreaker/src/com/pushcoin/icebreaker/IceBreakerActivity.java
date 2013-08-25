package com.pushcoin.icebreaker;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;
import android.os.AsyncTask;

public class IceBreakerActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView statusField = (TextView) findViewById(R.id.status_field);
		new DownloadHistoryTask(statusField).execute("mat here");
	}

	private static class DownloadHistoryTask extends AsyncTask<String, Void, Long>
	{
		DownloadHistoryTask(TextView statusField)
		{
			statusField_ = statusField;
		}

		protected void onPreExecute()
		{
			statusField_.setText("Loading...");
		}

		protected Long doInBackground(String... mat)
		{
			// simulate long fetch
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}

			return 0L;
		}

		protected void onPostExecute(Long result) 
		{
			statusField_.setText("as of Today 5:15 PM");
		}

		TextView statusField_;
	}

}
