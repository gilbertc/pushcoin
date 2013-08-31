package com.pushcoin.icebreaker;

public class Conf 
{
	static final int DSA_KEY_LENGTH = 1024;
	// PCOS doc names
	static final String PCOS_DOC_ERROR = "Error";
	static final String PCOS_DOC_REGISTER_ACK = "RegisterAck";

	// PCOS limits
	static final int PCOS_MAXLEN_TXN_ID = 20;
	static final int PCOS_MAXLEN_ERROR_MESSAGE = 256;

	// Preferences keys
	static final String PREFS_KEY_MAT_KEY = "mat";

	static final String STATUS_UNEXPECTED_HAPPENED = "Oops, it didn't work )-:";
}
