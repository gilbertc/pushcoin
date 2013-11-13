package com.pushcoin.srv.gateway.services;

import java.util.Date;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.pushcoin.srv.gateway.alarms.TransactionKeyAlarm;
import com.pushcoin.lib.core.data.Preferences;
import com.pushcoin.lib.core.data.TransactionKey;
import com.pushcoin.lib.core.net.PcosServer;
import com.pushcoin.lib.core.security.KeyStore;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.pcos.BlockWriter;
import com.pushcoin.lib.pcos.DocumentWriter;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;

public class TransactionKeyService extends WakefulIntentService {

	public static void scheduleAlarms(Context ctxt, long nextAlarmInMillis,
			boolean force) {

		WakefulIntentService.scheduleAlarms(new TransactionKeyAlarm(
				nextAlarmInMillis), ctxt, force);
	}

	private static Logger log = Logger.getLogger(TransactionKeyService.class);
	private static int RETRY_INTERVAL = 60000;

	private PcosServer server = null;

	public TransactionKeyService() {
		super("TransactionKeyService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		log.d("Key Fetching; ");

		if (server != null) {
			log.e("another fetch is already running");
			return;
		}

		if (Preferences.isDemoMode(this, false)){
			log.d("fetch skipped because demo mode");
			return;
		}

		String url = PcosServer.getDefaultUrl();
		KeyStore keyStore = KeyStore.getInstance(this);
		if (keyStore.hasMAT()) {

			try {
				DocumentWriter writer = new DocumentWriter("TxnKeyQuery");
				BlockWriter bo = new BlockWriter("Bo");
				bo.writeByteStr(keyStore.getMAT());
				writer.addBlock(bo);

				this.server = new PcosServer();
				this.server.postAsync(url, writer,
						new TransactionKeyQueryResponseListener(server));

			} catch (Exception ex) {
				log.e("exception when querying transaction key", ex);
				this.server = null;
			}
		}
	}

	public class TransactionKeyQueryResponseListener extends
			PcosServer.PcosResponseListener {
		public TransactionKeyQueryResponseListener(PcosServer server) {
			server.super();
		}

		@Override
		public void onErrorResponse(Object tag, byte[] trxId, long ec,
				String reason) {
			log.e("server returned error: " + reason);
			TransactionKey.setKeys(null);
			TransactionKeyService.scheduleAlarms(TransactionKeyService.this,
					RETRY_INTERVAL, false);
		}

		@Override
		public void onResponse(Object tag, InputDocument doc) {
			if (doc.getDocumentName().contains("TxnKeyOffer")) {
				try {
					InputBlock bo = doc.getBlock("Bo");

					Date lastExpire = new Date(0);

					long len = bo.readUint();
					TransactionKey[] keys = new TransactionKey[(int) len];
					for (int i = 0; i < len; ++i) {
						TransactionKey newKey = new TransactionKey();
						newKey.keyId = bo.readBytes(2);
						newKey.keyAttrs = bo.readString(0);
						newKey.expire = new Date(bo.readUlong() * 1000);
						newKey.key = bo.readByteStr(0);
						keys[i] = newKey;

						if (newKey.expire.after(lastExpire))
							lastExpire = newKey.expire;
					}
					TransactionKey.setKeys(keys);

					long diff = lastExpire.getTime() - new Date().getTime();
					if (len <= 0 || diff < 0) {
						log.e("invalid result from transaction keys: len="
								+ len + "; diff=" + diff);
						diff = RETRY_INTERVAL;
					}

					log.d("scheduling transaction key fetch in " + diff + "ms");

					TransactionKeyService.scheduleAlarms(
							TransactionKeyService.this, diff, false);

				} catch (Exception ex) {
					log.e("exception when processing transaction key response",
							ex);
					TransactionKeyService.scheduleAlarms(
							TransactionKeyService.this, RETRY_INTERVAL, false);
				}
			}
		}

		@Override
		public void onError(Object tag, Exception ex) {
			log.e("exception when getting transaction key response", ex);
			TransactionKeyService.scheduleAlarms(TransactionKeyService.this,
					RETRY_INTERVAL, false);
		}

		@Override
		public void onFinished(Object tag) {
			TransactionKeyService.this.server = null;
		}

	}
}
