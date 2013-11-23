package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class ChargeResult extends CallbackResult {
	public static final String KEY_REFDATA = "REFDATA";
	public static final String KEY_TRXID = "TRXID";
	public static final String KEY_ISAMOUNTEXACT = "ISAMOUNTEXACT";
	public static final String KEY_BALANCE = "BALANCE";
	public static final String KEY_UTC = "UTC";

	public ChargeResult() {
		this(new Bundle());
	}

	public ChargeResult(Bundle bundle) {
		super(bundle);
	}

	public byte[] getRefData() {
		return bundle.getByteArray(KEY_REFDATA);
	}

	public void setRefData(byte[] value) {
		bundle.putByteArray(KEY_REFDATA, value);
	}

	public String getTrxId() {
		return bundle.getString(KEY_TRXID);
	}

	public void setTrxId(String value) {
		bundle.putString(KEY_TRXID, value);
	}

	public Amount getBalance() {
		return bundle.getParcelable(KEY_BALANCE);
	}

	public void setBalance(Amount value) {
		bundle.putParcelable(KEY_BALANCE, value);
	}

	public boolean getIsAmountExact() {
		return bundle.getBoolean(KEY_ISAMOUNTEXACT);
	}

	public void setIsAmountExact(boolean value) {
		bundle.putBoolean(KEY_ISAMOUNTEXACT, value);
	}

	public long getUtc() {
		return bundle.getLong(KEY_UTC);
	}

	public void setUtc(long value) {
		bundle.putLong(KEY_UTC, value);
	}

}
