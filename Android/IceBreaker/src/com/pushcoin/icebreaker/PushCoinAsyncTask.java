package com.pushcoin.icebreaker;

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

abstract class PushCoinAsyncTask extends AsyncTask<String, Void, Void>
{
	static final String HTTP_API_URL = "https://api.minta.com/pcos/";
	static final int HTTP_API_MAX_RESPONSE_LEN = 4096;
	static final int HTTP_API_TIMEOUT = 3000; // ms
	static final String HTTP_API_USER_AGENT = "PushCoin-IceBreaker-v1";

	protected InputDocument invokeRemote(OutputDocument doc) throws PcosError, IOException
	{
		HttpClient httpClient = new DefaultHttpClient();

		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_API_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_API_TIMEOUT);

		HttpPost httpPost = new HttpPost(HTTP_API_URL);
		httpPost.setEntity( new ByteArrayEntity( doc.toBytes() ) );

		Log.v(Conf.TAG, HTTP_API_URL+doc.getDocumentName());
		HttpResponse res = httpClient.execute( httpPost );

		InputStream in = new BufferedInputStream(res.getEntity().getContent());

		byte[] resultBuf = new byte[HTTP_API_MAX_RESPONSE_LEN];
		int bytesLen = in.read(resultBuf);

		return new DocumentReader( resultBuf, bytesLen );
	}
}
