package com.pushcoin.core.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.pushcoin.core.exceptions.ServerException;
import com.pushcoin.core.security.SSLSocketFactory;
import com.pushcoin.core.utils.Logger;
import com.pushcoin.core.utils.Stringifier;

public class Server {
	private static Logger log = Logger.getLogger(Server.class);

	public enum ErrorCode {

		EC_KeyStoreException(1002), EC_NoSuchAlgorithmException(1003), EC_CertificateException(
				1004), EC_UnknownHostException(1005), EC_IOException(1006), EC_KeyManagementException(
				1007), EC_UnrecoverableKeyException(1008), EC_UnknownException(
				1009);

		public int code;

		private ErrorCode(int code) {
			this.code = code;
		}
	}

	public static boolean isNetworkAvailable(Context ctxt) {
		ConnectivityManager connectivityManager = (ConnectivityManager) ctxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private static String defaultUrl = "";

	public static void setDefaultUrl(String url) {
		defaultUrl = url;
	}

	public static String getDefaultUrl() {
		return defaultUrl;
	}

	public abstract class ResponseListener {
		public abstract void onError(Object tag, Exception ex);

		public abstract void onResponse(Object tag, byte[] res);

		public void onFinished(Object tag) {
		}
	}

	public void postAsync(String url, byte[] data, ResponseListener listener) {
		postAsync(null, url, data, listener);
	}

	public void postAsync(Object tag, String url, byte[] data,
			ResponseListener listener) {

		AsyncPostParams params = new AsyncPostParams();
		params.tag = tag;
		params.url = url;
		params.data = data;
		params.listener = listener;

		new PostTask().execute(params);
	}

	public byte[] post(String url, byte[] data) throws Exception {
		return post(null, url, data);
	}

	public byte[] post(Object tag, String url, byte[] data) throws Exception {

		PostParams params = new PostParams();
		params.tag = tag;
		params.url = url;
		params.data = data;

		return post(params).data;
	}

	private class PostParams {
		public Object tag;
		public String url;
		public byte[] data;
	}

	private class AsyncPostParams extends PostParams {
		public ResponseListener listener;
	}

	private class PostResult {

		public PostParams postParams;
		public byte[] data;
		public Exception ex;

		boolean isOK() {
			return ex == null;
		}

		public PostResult(PostParams params, byte[] data) {
			this.postParams = params;
			this.ex = null;
			this.data = data;
		}

		public PostResult(PostParams params, Exception ex) {
			this.postParams = params;
			this.ex = ex;
			this.data = null;
		}

	}

	private class PostTask extends AsyncTask<AsyncPostParams, Void, PostResult> {
		protected PostResult doInBackground(AsyncPostParams... params) {
			try {
				return post(params[0]);
			} catch (ServerException e) {
				log.e("PostTask", e);
				return new PostResult(params[0], e);
			}
		}

		protected void onPostExecute(PostResult result) {
			AsyncPostParams params = (AsyncPostParams) result.postParams;
			if (params.listener != null) {

				if (result.isOK())
					params.listener.onResponse(params.tag, result.data);
				else
					params.listener.onError(params.tag, result.ex);

				params.listener.onFinished(params.tag);
			}
		}
	}

	private PostResult post(PostParams params) throws ServerException {

		if (params.data != null)
			log.d("Sending: " + Stringifier.toString(params.data));
		try {
			KeyStore trustStore;
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf;
			sf = new SSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams httpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

			// Set the timeout in milliseconds until a connection is
			// established.
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParams,
					timeoutConnection);

			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					httpParams, registry);

			HttpClient httpclient = new DefaultHttpClient(ccm, httpParams);
			HttpPost httppost = new HttpPost(params.url);

			ByteArrayEntity bytesentity = new ByteArrayEntity(params.data);
			httppost.setEntity(bytesentity);

			// Execute HTTP Post Request
			HttpResponse response;
			response = httpclient.execute(httppost);

			ByteArrayOutputStream bao = new ByteArrayOutputStream();

			response.getEntity().writeTo(bao);

			byte[] res = bao.toByteArray();

			log.d("Receiving: " + Stringifier.toString(res));

			return new PostResult(params, res);

		} catch (KeyStoreException e) {
			log.e("KeyStoreException", e);
			throw new ServerException(ErrorCode.EC_KeyStoreException);
		} catch (NoSuchAlgorithmException e) {
			log.e("NoSuchAlgorithmException", e);
			throw new ServerException(ErrorCode.EC_NoSuchAlgorithmException);
		} catch (CertificateException e) {
			log.e("CertificateException", e);
			throw new ServerException(ErrorCode.EC_CertificateException);
		} catch (UnknownHostException e) {
			log.e("UnknownHostException", e);
			throw new ServerException(ErrorCode.EC_UnknownHostException);
		} catch (IOException e) {
			log.e("IOException", e);
			throw new ServerException(ErrorCode.EC_IOException);
		} catch (KeyManagementException e) {
			log.e("KeyManagementException", e);
			throw new ServerException(ErrorCode.EC_KeyManagementException);
		} catch (UnrecoverableKeyException e) {
			log.e("UnrecoverableKeyException", e);
			throw new ServerException(ErrorCode.EC_UnrecoverableKeyException);
		} catch (Exception e) {
			log.e("Exception", e);
			throw new ServerException(ErrorCode.EC_UnknownException);
		}

	}

}
