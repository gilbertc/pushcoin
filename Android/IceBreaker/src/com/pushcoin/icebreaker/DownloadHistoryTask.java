package com.pushcoin.icebreaker;

import android.app.Activity;
import android.content.res.Resources;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RatingBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;

class DownloadHistoryTask extends AsyncTask<String, Void, Void>
{
	static final String HTTP_API_URL = "https://api.minta.com/pcos/";
	static final int HTTP_API_MAX_RESPONSE_LEN = 4096;
	static final int HTTP_API_TIMEOUT = 3000; // ms
	static final String HTTP_API_USER_AGENT = "PushCoin-IceBreaker-v1";

	DownloadHistoryTask( IceBreakerActivity model )
	{
		// Stores fetched data. Must only access from UI thread!
		model_ = model;
	}

	protected void onPreExecute()
	{
		// notify model handlers data is being fetched
	}

	protected Void doInBackground(String... mat)
	{
		try 
		{
			// Request body block
			OutputBlock out_bo = new BlockWriter( "Bo" );

			// MAT
			out_bo.writeByteStr( Binascii.unhexlify( mat[0] ) );
			
			// Page size and offset
			out_bo.writeUint( 0 );
			out_bo.writeUint( 10 );

			OutputDocument req = new DocumentWriter("TxnHistoryQuery");
			req.addBlock(out_bo);

			// ship over HTTPS
			InputDocument res = invokeRemote( req );
			if (res != null) 
			{
				model_.setStatus( "as of Today 5:15 PM" );
			}
		} catch (PcosError e) {
			model_.setStatus( e.reason );
		}
		catch (Exception e) {
			model_.setStatus( e.getMessage() );
		}
		return null;
	}

	private InputDocument invokeRemote(OutputDocument doc) throws PcosError, IOException
	{
		Log.v(TAG, "create http client");
		HttpClient httpClient = new DefaultHttpClient();

		Log.v(TAG, "build URL, params");
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_API_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_API_TIMEOUT);

		HttpPost httpPost = new HttpPost(HTTP_API_URL);
		httpPost.setEntity( new ByteArrayEntity( doc.toBytes() ) );

		Log.v(TAG, "invoke");
		HttpResponse res = httpClient.execute( httpPost );

		Log.v(TAG, "read response");
		InputStream in = new BufferedInputStream(res.getEntity().getContent());

		Log.v(TAG, "read results");
		byte[] resultBuf = new byte[HTTP_API_MAX_RESPONSE_LEN];
		int bytesLen = in.read(resultBuf);

		return new DocumentReader( resultBuf, bytesLen );
	}

	protected void onPostExecute(Void v) 
	{
		// notify model handlers we have the data
	}

	private static final String TAG = "IceBr|DH";
	private final IceBreakerActivity model_;
}
