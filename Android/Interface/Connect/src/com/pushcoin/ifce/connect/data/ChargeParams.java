package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class ChargeParams extends CallbackParams {
	public static final String KEY_REFDATA = "REFDATA";
	public static final String KEY_PAYMENT = "PAYMENT";
	public static final String KEY_TAX = "TAX";
	public static final String KEY_TIPS = "TIPS";
	public static final String KEY_PASSCODE = "PASSCODE";
	public static final String KEY_CURRENCY = "CURRENCY";
	public static final String KEY_INVOICE = "INOVICE";
	public static final String KEY_NOTE = "NOTE";
	public static final String KEY_GEOLOCATION = "GEOLOCATION";
	public static final String KEY_ACCOUNT_ID = "ACCOUNT_ID";

	public ChargeParams() {
		this(new Bundle());
	}

	public ChargeParams(Bundle bundle) {
		super(bundle);
	}

	public String getRefData() {
		return bundle.getString(KEY_REFDATA, "");
	}

	public void setRefData(String value) {
		bundle.putString(KEY_REFDATA, value);
	}

	public Amount getPayment() {
		return bundle.getParcelable(KEY_PAYMENT);
	}

	public void setPayment(Amount value) {
		bundle.putParcelable(KEY_PAYMENT, value);
	}

	public Amount getTax() {
		return bundle.getParcelable(KEY_TAX);
	}

	public void setTax(Amount value) {
		bundle.putParcelable(KEY_TAX, value);
	}

	public Amount getTips() {
		return bundle.getParcelable(KEY_TIPS);
	}

	public void setTips(Amount value) {
		bundle.putParcelable(KEY_TIPS, value);
	}

	public String getPasscode() {
		return bundle.getString(KEY_PASSCODE, "");
	}

	public void setPasscode(String value) {
		bundle.putString(KEY_PASSCODE, value);
	}

	public String getCurrency() {
		return bundle.getString(KEY_CURRENCY, "USD");
	}

	public void setCurrency(String value) {
		bundle.putString(KEY_CURRENCY, value);
	}

	public String getInvoice() {
		return bundle.getString(KEY_INVOICE, "");
	}

	public void setInvoice(String value) {
		bundle.putString(KEY_INVOICE, value);
	}

	public String getNote() {
		return bundle.getString(KEY_NOTE, "");
	}

	public void setNote(String value) {
		bundle.putString(KEY_NOTE, value);
	}

	public String getAccountId() {
		return bundle.getString(KEY_ACCOUNT_ID, "");
	}

	public void setAccountId(String value) {
		bundle.putString(KEY_ACCOUNT_ID, value);
	}

	public GeoLocation getGeoLocation() {
		return bundle.getParcelable(KEY_GEOLOCATION);
	}

	public void setGeoLocation(GeoLocation value) {
		bundle.putParcelable(KEY_GEOLOCATION, value);
	}

}
