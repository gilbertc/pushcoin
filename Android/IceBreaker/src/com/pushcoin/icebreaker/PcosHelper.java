package com.pushcoin.icebreaker;

import android.util.Log;
import com.pushcoin.Binascii;
import com.pushcoin.pcos.*;
import android.content.Context;
import android.text.format.DateUtils;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Pattern;
import java.text.NumberFormat;

class PcosHelper
{
	/**
		Error message
	*/
	static public class ErrorInfo
	{
		String txnId;
		long errorCode;
		String message;
	}

	static ErrorInfo parseError(InputDocument doc) throws PcosError
	{
		try 
		{
			if ( doc.getDocumentName().equals( Conf.PCOS_DOC_ERROR) )
			{
				InputBlock bo = doc.getBlock("Bo");	
				ErrorInfo res = new ErrorInfo();
				res.txnId = new String(bo.readByteStr(Conf.PCOS_MAXLEN_TXN_ID), "UTF-8");
				res.errorCode = bo.readUint();
				res.message = bo.readString(Conf.PCOS_MAXLEN_ERROR_MESSAGE);
				return res;
			}
		} catch (UnsupportedEncodingException e) {}
		throw new PcosError(PcosErrorCode.ERR_MALFORMED_MESSAGE, "Cannot parse " + doc.getDocumentName());
	}

	/**
		Register new device
	*/
	static public class RegisterAckResult
	{
		byte[] mat;
	}

	static RegisterAckResult parseRegisterAck(InputDocument doc) throws PcosError
	{
		if ( doc.getDocumentName().equals( Conf.PCOS_DOC_REGISTER_ACK) )
		{
			InputBlock bo = doc.getBlock("Bo");	
			RegisterAckResult res = new RegisterAckResult();
			res.mat = bo.readByteStr(Conf.PCOS_MAXLEN_TXN_ID);
			return res;
		}
		throw new PcosError(PcosErrorCode.ERR_MALFORMED_MESSAGE, "Cannot parse " + doc.getDocumentName());
	}

	/**
		Transaction history with current balance
		
		Helper classes:
			Address
			TransactionInfo
	*/

	static public class Address
	{
		String street;
		String city;
		String state;
		String zipCode;
		String country;
	}

	static public class TransactionInfo
	{
		String txnId;
		String txnType;
		String txnContext;
		String txnStatus;
		long txnTimeEpoch;
		int txnRating;
		String deviceName;
		String currency;
		BigDecimal amount;
		BigDecimal tax;
		BigDecimal tip;
		String counterParty;
		String invoice;
		String note;
		Address posAddress;
		String merchantPhone;
		String merchantEmail;
		double posLatitude;
		double posLongitude;
		float merchantScore;
		int totalVotes;
	}

	static public class TxnHistoryReply
	{
		BigDecimal balance;
		long balanceTimeEpoch;

		// all transactions found, useful for paging: "N of <total>"
		long totalTransactions;

		TransactionInfo[] transactionInfo;
	}

	static TxnHistoryReply parseTxnHistoryReply(InputDocument doc) throws PcosError
	{
		if ( doc.getDocumentName().equals( Conf.PCOS_DOC_TXN_HISTORY_REPLY) )
		{
			TxnHistoryReply res = new TxnHistoryReply();

			// Balance segment
			//
			InputBlock bl = doc.getBlock("Bl");	

			res.balance = fromScaledValue( bl.readLong(), bl.readInt() );
			res.balanceTimeEpoch = bl.readUlong();

			// Transaction history segment
			//
			InputBlock tr = doc.getBlock("Tr");	

			// account transactions count
			res.totalTransactions = tr.readUint();

			// transactions returned this time around
			int trxCount = (int) tr.readUint();
			res.transactionInfo = new TransactionInfo[trxCount];

			for (int i = 0; i < trxCount; ++i)
			{
				TransactionInfo	record = new TransactionInfo();
				res.transactionInfo[i] = record;

				record.txnId = tr.readString(Conf.PCOS_MAXLEN_TXN_ID);
				record.deviceName = tr.readString(Conf.PCOS_MAXLEN_DEVICE_NAME);
				record.txnTimeEpoch = tr.readUlong();
				record.txnType = tr.readString(1);
				record.txnContext = tr.readString(1);
				record.currency = tr.readString(Conf.PCOS_MAXLEN_CURRENCY);
				record.amount = fromScaledValue( tr.readLong(), tr.readInt() );

				// reported tax
				if ( tr.readBool() ) {
					record.tax = fromScaledValue( tr.readLong(), tr.readInt() );
				} else {
					record.tax = BigDecimal.ZERO;
				}

				// reported tip
				if (tr.readBool()) {
					record.tip = fromScaledValue( tr.readLong(), tr.readInt() );
				} else {
					record.tip = BigDecimal.ZERO;
				}

				record.counterParty = tr.readString(Conf.PCOS_MAXLEN_COUNTERPARTY);
				record.invoice = tr.readString(Conf.PCOS_MAXLEN_INVOICE);
				record.note = tr.readString(Conf.PCOS_MAXLEN_TXN_NOTE);

				// address of the POS station
				record.posAddress = new Address();
				if ( tr.readBool() )
				{
					record.posAddress.street = tr.readString(Conf.PCOS_MAXLEN_ADDRESS_STREET);
					record.posAddress.city = tr.readString(Conf.PCOS_MAXLEN_ADDRESS_CITY);
					record.posAddress.state = tr.readString(Conf.PCOS_MAXLEN_ADDRESS_STATE);
					record.posAddress.zipCode = tr.readString(Conf.PCOS_MAXLEN_ADDRESS_CODE);
					record.posAddress.country = tr.readString(Conf.PCOS_MAXLEN_ADDRESS_COUNTRY);
				}

				// contact info
				if ( tr.readBool() )
				{
					record.merchantPhone = tr.readString(Conf.PCOS_MAXLEN_PHONE);
					record.merchantEmail = tr.readString(Conf.PCOS_MAXLEN_WEBSITE);
				} else {
					record.merchantPhone = "";
					record.merchantEmail = "";
				}

				// geo location
				if ( tr.readBool() )
				{
					record.posLatitude = tr.readDouble();
					record.posLongitude = tr.readDouble();
				} else {
					record.posLatitude = Double.NaN;
					record.posLongitude = Double.NaN;
				}

				record.txnStatus = tr.readString(1);
				record.txnRating = (int)tr.readUint();
				record.merchantScore = (float)tr.readDouble();
				record.totalVotes = (int)tr.readUint();
			}
			return res;
		}
		throw new PcosError(PcosErrorCode.ERR_MALFORMED_MESSAGE, "Cannot parse " + doc.getDocumentName());
	}

	static BigDecimal fromScaledValue(long unscaledVal, int scale)
	{
		// We negate the scale here to undo BigDecimal-constructor's
		// negation of the scale param. Oh well.
		return BigDecimal.valueOf(unscaledVal, -scale);
	}

	static Date fromEpochUtc(long val)
	{
		return new Date( val * 1000 );	
	}

	static long getEpochUtc()
	{
		return System.currentTimeMillis() / 1000;
	}

	static String prettyTime( Context ctx, long sinceEpoch )
	{
		return DateUtils.getRelativeDateTimeString( ctx, sinceEpoch * 1000, 
				Conf.STATUS_MIN_RESOLUTION, Conf.STATUS_TRANSITION_RESOLUTION, Conf.STATUS_FLAGS ).toString();
	}

	static public class DateTimePair
	{
		String date;
		String time;
	}

	static final Pattern dateSplitRegEx = Pattern.compile(", ");
	static DateTimePair prettyTimeParts( Context ctx, long sinceEpoch )
	{
		DateTimePair res = new DateTimePair();
		String[] together = dateSplitRegEx.split(prettyTime(ctx, sinceEpoch));
		if (together.length > 1)
		{
			res.date = together[0];
			res.time = together[1];
		} 
		else
		{
			res.date = together[0];
			res.time = "";
		}
		return res;
	}

	static String prettyAmount( BigDecimal val, String currency )
	{
		return NumberFormat.getCurrencyInstance().format(val);
	}
}
