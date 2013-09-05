package com.pushcoin.core.net;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Server {

	Server() {

		try {
			URL url = new URL("https://api.minta.com/pcos");
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.
		} catch (Exception ex) {

		}

	}

}
