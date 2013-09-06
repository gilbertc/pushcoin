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
