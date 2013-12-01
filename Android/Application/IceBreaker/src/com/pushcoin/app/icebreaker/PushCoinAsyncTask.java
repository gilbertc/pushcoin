/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	public class ResultByteBuffer
	{
		byte[] dest = new byte[Conf.HTTP_API_MAX_RESPONSE_LEN];;
		int bytesRead = 0;
	};

	protected InputDocument invokeRemote( OutputDocument doc, ResultByteBuffer resBuffer ) throws PcosError, IOException
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
		resBuffer.bytesRead = in.read(resBuffer.dest);

		return new DocumentReader( resBuffer.dest, resBuffer.bytesRead );
	}
}
