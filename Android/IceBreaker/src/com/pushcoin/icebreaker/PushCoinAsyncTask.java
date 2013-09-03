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
	protected InputDocument invokeRemote(OutputDocument doc) throws PcosError, IOException
	{
		HttpClient httpClient = new DefaultHttpClient();

		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, Conf.HTTP_API_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, Conf.HTTP_API_TIMEOUT);

		HttpPost httpPost = new HttpPost(Conf.HTTP_API_URL);
		httpPost.setEntity( new ByteArrayEntity( doc.toBytes() ) );

		Log.v(Conf.TAG, Conf.HTTP_API_URL+doc.getDocumentName());
		HttpResponse res = httpClient.execute( httpPost );

		InputStream in = new BufferedInputStream(res.getEntity().getContent());

		byte[] resultBuf = new byte[Conf.HTTP_API_MAX_RESPONSE_LEN];
		int bytesLen = in.read(resultBuf);

		return new DocumentReader( resultBuf, bytesLen );
	}
}
