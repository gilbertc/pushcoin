package com.pushcoin.icebreaker;

import android.text.format.DateUtils;

public class Conf 
{
	static final String HTTP_API_URL = "https://api.pushcoin.com/pcos/";
	//static final String HTTP_API_URL = "https://api.minta.com/pcos/";
	static final int HTTP_API_MAX_RESPONSE_LEN = 10*1024;
	static final int HTTP_API_TIMEOUT = 4000; // ms

	// Number of transactions in history
	static final int TRANSACTION_HISTORY_SIZE=20;
	
	// Local filename where we store downloaded history data
	static final String CACHED_HISTORY_FILENAME = "history_cache";

	// Request throttling
	static final long THROTTLE_MAX_REQUESTS_PER_WINDOW = 1;
	static final long THROTTLE_REQUEST_WINDOW_DURATION = 10;

	// Disable user rating for transactions older than this many seconds.
	static final int USER_RATING_CUTOFF = 24*3600; // one day

	// The minimum elapsed time (in milliseconds) to report when showing relative times. 
	// For example, a time 3 seconds in the past will be reported as "0 minutes ago" 
	// if this is set to MINUTE_IN_MILLIS.
	static final long STATUS_MIN_RESOLUTION = DateUtils.DAY_IN_MILLIS;

	// The elapsed time (in milliseconds) at which to stop reporting relative measurements.
	// For example, will transition from "6 days ago" to "Dec 12" when using WEEK_IN_MILLIS. 
	static final long STATUS_TRANSITION_RESOLUTION = 2*24*3600*1000;

	// The flags argument is a bitmask of options found in DateUtils class
	static final int STATUS_FLAGS = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_12HOUR;

	static final int DSA_KEY_LENGTH = 1024;
	// PCOS doc names
	static final String PCOS_DOC_SUCCESS = "Ok";
	static final String PCOS_DOC_ERROR = "Error";
	static final String PCOS_DOC_REGISTER_ACK = "RegisterAck";
	static final String PCOS_DOC_TXN_HISTORY_REPLY = "TxnHistoryReply";

	// Error codes
	static final int PCOS_ERROR_DEVICE_NOT_ACTIVE = 1107;

	// PCOS limits
	static final int PCOS_MAXLEN_TXN_ID = 20;
	static final int PCOS_MAXLEN_TXN_NOTE = 127+64;
	static final int PCOS_MAXLEN_CURRENCY = 3;
	static final int PCOS_MAXLEN_ERROR_MESSAGE = 256;
	static final int PCOS_MAXLEN_DEVICE_NAME = 40;
	static final int PCOS_MAXLEN_INVOICE = 24;
	static final int PCOS_MAXLEN_ACCOUNT_ID = 10;
	static final int PCOS_MAXLEN_ADDRESS_STREET = 64;
	static final int PCOS_MAXLEN_ADDRESS_CITY = 64;
	static final int PCOS_MAXLEN_ADDRESS_STATE = 64;
	static final int PCOS_MAXLEN_ADDRESS_CODE = 15;
	static final int PCOS_MAXLEN_ADDRESS_COUNTRY = 2;
	static final int PCOS_MAXLEN_PHONE = 20;
	static final int PCOS_MAXLEN_WEBSITE = 64;
	static final int PCOS_MAXLEN_COUNTERPARTY = 64;

	// Preferences keys
	static final String PREFS_KEY_MAT_KEY = "mat";

	static final String STATUS_UNEXPECTED_HAPPENED = "Oops, it didn't work )-:";
	static final String DEFAULT_CURRENCY = "USD";

	static final String TAG = "PushCoin";

	static final String TRANSACTION_TYPE_CREDIT = "C";
	static final String TRANSACTION_TYPE_DEBIT = "D";
}
