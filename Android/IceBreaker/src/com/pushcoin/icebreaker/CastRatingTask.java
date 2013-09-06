package com.pushcoin.icebreaker;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;

class CastRatingTask extends PushCoinAsyncTask
{
	CastRatingTask( IceBreakerActivity model, byte[] mat, String txnId, int rating )
	{
		// Stores fetched data. Must only access from UI thread!
		model_ = model;
		mat_ = mat;
		txnId_ = txnId;
		rating_ = rating;
	}

	@Override
	protected void onPreExecute() { }

	protected Void doInBackground(String... none)
	{
		try 
		{
			// Request body block
			OutputBlock out_bo = new BlockWriter( "Bo" );

			
			out_bo.writeByteStr( mat_ );
			out_bo.writeString( txnId_ );
			out_bo.writeUint( rating_ );
			
			OutputDocument req = new DocumentWriter("TxnRating");
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
				}
				else if (docName.equals( Conf.PCOS_DOC_SUCCESS ) ) {
					Log.i( Conf.TAG, "vote-accepted|txn="+txnId_+";rating="+rating_);
				}
				else // Not an Error nor Ack!?
				{
					Log.e( Conf.TAG, "unexpected-server-response|doc="+docName );
				}
			}
		} catch (Exception e) {
			Log.e( Conf.TAG, "voting-error|err="+e.getMessage() );
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void v) {
	}

	private final IceBreakerActivity model_;
	private final byte[] mat_;
	private final String txnId_;
	private final int rating_;
}
