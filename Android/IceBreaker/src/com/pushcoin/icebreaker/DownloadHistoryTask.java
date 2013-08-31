package com.pushcoin.icebreaker;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;

class DownloadHistoryTask extends PushCoinAsyncTask
{
	DownloadHistoryTask( IceBreakerActivity model, byte[] mat ) {
		// Stores fetched data. Must only access from UI thread!
		model_ = model;
		mat_ = mat;
	}

	protected void onPreExecute()
	{
		// notify model handlers data is being fetched
	}

	protected Void doInBackground(String... none)
	{
		try 
		{
			// Request body block
			OutputBlock out_bo = new BlockWriter( "Bo" );

			// MAT
			out_bo.writeByteStr( mat_ );
			
			// Page size and offset
			out_bo.writeUint( 0 );
			out_bo.writeUint( 10 );

			OutputDocument req = new DocumentWriter("TxnHistoryQuery");
			req.addBlock(out_bo);

			// ship over HTTPS
			InputDocument res = invokeRemote( req );
			if (res != null) 
			{
				status_ = "as of Today 5:15 PM";
			}
		} catch (Exception e) {
			status_ = e.getMessage();
		}
		return null;
	}

	protected void onPostExecute(Void v) 
	{
		// notify model we have the data
		model_.beginModelUpdates();
		model_.setStatus("as of Today 5:15 PM");
		model_.endModelUpdates();
	}

	private static final String TAG = "TxnHistoryQuery|";
	private final IceBreakerActivity model_;
	private final byte[] mat_;
	private String status_;
}
