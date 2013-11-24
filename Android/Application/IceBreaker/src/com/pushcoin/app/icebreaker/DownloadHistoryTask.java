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

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;
import java.io.File;

class DownloadHistoryTask extends PushCoinAsyncTask
{
	DownloadHistoryTask( IceBreakerActivity model, byte[] mat, PcosHelper.TxnHistoryReply oldData ) {
		// Stores fetched data. Must only access from UI thread!
		model_ = model;
		mat_ = mat;

		// While we are waiting for download, it's OK to show old results
		if (oldData == null)
		{
			try 
			{
				byte[] readData = new byte[Conf.HTTP_API_MAX_RESPONSE_LEN];
				int bytesRead = PcosHelper.loadFromFile( new File( model_.getCacheDir(), Conf.CACHED_HISTORY_FILENAME), readData ); 
				if (bytesRead > 0)
				{
					Message m = Message.obtain();
					m.what = MessageId.ACCOUNT_HISTORY_REPLY;
					m.obj = PcosHelper.parseTxnHistoryReply(new DocumentReader( readData, bytesRead ));
					model_.post(m);
				}
			} catch (PcosError e) { 
				Log.e( Conf.TAG, "error-loading-cached-history|"+e.getMessage() );
			}
		} 
		else 
		{
			Message m = Message.obtain();
			m.what = MessageId.ACCOUNT_HISTORY_REPLY;
			m.obj = oldData;
			model_.post(m);
		}
	}

	@Override
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
			out_bo.writeUint( Conf.TRANSACTION_HISTORY_SIZE );

			OutputDocument req = new DocumentWriter("TxnHistoryQuery");
			req.addBlock(out_bo);

			// call server
			PushCoinAsyncTask.ResultByteBuffer resultBuf = new PushCoinAsyncTask.ResultByteBuffer();
			InputDocument res = invokeRemote( req, resultBuf );
			if (res != null) 
			{
				String docName = res.getDocumentName();
				if ( docName.equals( Conf.PCOS_DOC_ERROR ) )
				{
					PcosHelper.ErrorInfo err = PcosHelper.parseError( res );
					Log.e( Conf.TAG, "reason="+err.message+";code="+err.errorCode );

					// If device was disabled, we need to go back to Setup mode
					if (err.errorCode == Conf.PCOS_ERROR_DEVICE_NOT_ACTIVE)
					{
						Message m = Message.obtain();
						m.what = MessageId.DEVICE_NOT_ACTIVE;
						model_.post(m);
					}

					if (err.message.isEmpty()) {
						status_ = Conf.STATUS_UNEXPECTED_HAPPENED;
					}
					else {
						status_ = err.message;
					}
				}
				else if (docName.equals( Conf.PCOS_DOC_TXN_HISTORY_REPLY ) )
				{
					// store result locally in case we go offline
					// and want to show something
					PcosHelper.saveToFile( new File(model_.getCacheDir(), Conf.CACHED_HISTORY_FILENAME), resultBuf.dest, resultBuf.bytesRead ); 

					// post event data arrived
					Message m = Message.obtain();
					m.what = MessageId.ACCOUNT_HISTORY_REPLY;
					m.obj = PcosHelper.parseTxnHistoryReply(res);
					model_.post(m);
				}
				else // Not an Error nor Ack!?
				{
					Log.e( Conf.TAG, "unexpected-server-response|doc="+docName );
					status_ = Conf.STATUS_UNEXPECTED_HAPPENED;
				}
			}
		} catch (Exception e) {
			status_ = e.getMessage();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void v) 
	{
		// may need to display an error
		if ( !status_.isEmpty() ) {
			model_.setStatus(status_);
		}

		// download is done, stop busy-indicators
		Message m = Message.obtain();
		m.what = MessageId.ACCOUNT_HISTORY_STOPPED;
		model_.post(m);
	}

	private final IceBreakerActivity model_;
	private final byte[] mat_;
	private String status_ = "";
}
