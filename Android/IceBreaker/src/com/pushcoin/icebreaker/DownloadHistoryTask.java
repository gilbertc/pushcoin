package com.pushcoin.icebreaker;

import android.os.Bundle;
import android.os.Message;
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
		// handlers may want to show a busy-status..
		Message m = Message.obtain();
		m.what = MessageId.ACCOUNT_HISTORY_PENDING;
		model_.post(m);
		// Start clean
		status_ = "";
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

			// call server
			InputDocument res = invokeRemote( req );
			if (res != null) 
			{
				String docName = res.getDocumentName();
				if ( docName.equals( Conf.PCOS_DOC_ERROR ) )
				{
					PcosHelper.ErrorInfo err = PcosHelper.parseError( res );
					Log.e( TAG, "reason="+err.message+";code="+err.errorCode );
					if (err.message.isEmpty()) {
						status_ = Conf.STATUS_UNEXPECTED_HAPPENED;
					}
					else {
						status_ = err.message;
					}
				}
				else if (docName.equals( Conf.PCOS_DOC_TXN_HISTORY_REPLY ) )
				{
					Message m = Message.obtain();
					m.what = MessageId.ACCOUNT_HISTORY_REPLY;
					m.obj = PcosHelper.parseTxnHistoryReply(res);
					model_.post(m);
				}
				else // Not an Error nor Ack!?
				{
					Log.e( TAG, "unexpected-server-response|doc="+docName );
					status_ = Conf.STATUS_UNEXPECTED_HAPPENED;
				}
			}
		} catch (Exception e) {
			status_ = e.getMessage();
		}
		return null;
	}

	protected void onPostExecute(Void v) 
	{
		// may need to display an error
		if ( !status_.isEmpty() ) 
		{
			model_.beginModelUpdates();
			model_.setStatus(status_);
			model_.endModelUpdates();
		}

		// download is done, stop busy-indicators
		Message m = Message.obtain();
		m.what = MessageId.ACCOUNT_HISTORY_STOPPED;
		model_.post(m);
	}

	private static final String TAG = "TxnHistoryQuery|";
	private final IceBreakerActivity model_;
	private final byte[] mat_;
	private String status_;
}
