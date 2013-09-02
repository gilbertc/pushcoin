package com.pushcoin.icebreaker;

import android.text.format.DateUtils;

public class Conf 
{
	// Request throttling
	static final long THROTTLE_MAX_REQUESTS_PER_WINDOW = 1;
	static final long THROTTLE_REQUEST_WINDOW_DURATION = 10;

	// The minimum elapsed time (in milliseconds) to report when showing relative times. 
	// For example, a time 3 seconds in the past will be reported as "0 minutes ago" 
	// if this is set to MINUTE_IN_MILLIS.
	static final long STATUS_MIN_RESOLUTION = DateUtils.DAY_IN_MILLIS;

	// The elapsed time (in milliseconds) at which to stop reporting relative measurements.
	// For example, will transition from "6 days ago" to "Dec 12" when using WEEK_IN_MILLIS. 
	static final long STATUS_TRANSITION_RESOLUTION = DateUtils.WEEK_IN_MILLIS;

	// The flags argument is a bitmask of options found in DateUtils class
	static final int STATUS_FLAGS = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_12HOUR;

	static final int DSA_KEY_LENGTH = 1024;
	// PCOS doc names
	static final String PCOS_DOC_ERROR = "Error";
	static final String PCOS_DOC_REGISTER_ACK = "RegisterAck";
	static final String PCOS_DOC_TXN_HISTORY_REPLY = "TxnHistoryReply";

	// PCOS limits
	static final int PCOS_MAXLEN_TXN_ID = 20;
	static final int PCOS_MAXLEN_TXN_NOTE = 127;
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

	// Preferences keys
	static final String PREFS_KEY_MAT_KEY = "mat";

	static final String STATUS_UNEXPECTED_HAPPENED = "Oops, it didn't work )-:";
	static final String DEFAULT_CURRENCY = "USD";
}
