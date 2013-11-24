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

import android.util.Log;
import android.os.Message;
import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;

class RegisterDeviceTask extends PushCoinAsyncTask
{
	RegisterDeviceTask( IceBreakerActivity model ) {
		// Stores fetched data. Must only access from UI thread!
		model_ = model;
	}

	protected void onPreExecute()
	{
		Message m = Message.obtain();
		m.what = MessageId.REGISTER_DEVICE_PENDING;
		model_.post(m);
		// Start clean
		status_ = "";
	}

	protected Void doInBackground(String... activationCode)
	{
		try 
		{
			// Request body block
			OutputBlock out_bo = new BlockWriter( "Bo" );

			// Device activation code
			out_bo.writeString( activationCode[0] );
			
			// DSA pub key
			CryptoHelper.DsaKeyPair key = CryptoHelper.generateDsaKeyPair();
			byte[] encodedPubKey = key.publicKey.getEncoded();
			out_bo.writeByteStr( encodedPubKey );

			OutputDocument req = new DocumentWriter("Register");
			req.addBlock(out_bo);

			// ship over HTTPS
			PushCoinAsyncTask.ResultByteBuffer resultBuf = new PushCoinAsyncTask.ResultByteBuffer();
			InputDocument res = invokeRemote( req, resultBuf );
			if (res != null) 
			{
				String docName = res.getDocumentName();
				if ( docName.equals( Conf.PCOS_DOC_ERROR ) )
				{
					PcosHelper.ErrorInfo err = PcosHelper.parseError( res );
					Log.e( Conf.TAG, "reason="+err.message+";code="+err.errorCode );
					if (err.message.isEmpty()) {
						status_ = Conf.STATUS_UNEXPECTED_HAPPENED;
					}
					else {
						status_ = err.message;
					}
				}
				else if (docName.equals( Conf.PCOS_DOC_REGISTER_ACK ) )
				{
					Message m = Message.obtain();
					m.what = MessageId.REGISTER_DEVICE_SUCCESS;
					m.obj = PcosHelper.parseRegisterAck(res);
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

	protected void onPostExecute(Void v) 
	{
		// notify model we have the data
		if ( !status_.isEmpty() ) {
			model_.setStatus(status_);
		}

		Message m = Message.obtain();
		m.what = MessageId.REGISTER_DEVICE_STOPPED;
		model_.post(m);
	}

	private final IceBreakerActivity model_;
	String status_ = "";
}
