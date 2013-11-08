package com.pushcoin.ifce.connect.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ChargeParams implements Parcelable {

	public String refData = "";
	public Amount payment = new Amount();
	public Amount tax = null;
	public Amount tips = null;
	public String passcode = "";
	public String currency = "USD";
	public String invoice = "";
	public String note = "";
	public GeoLocation geoLocation = null;
	
	public ChargeParams()
	{
		
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(refData);
		dest.writeParcelable(payment, flags);

		if (tax != null) {
			dest.writeByte((byte) 1);
			dest.writeParcelable(tax, flags);
		} else {
			dest.writeByte((byte) 0);
		}

		if (tips != null) {
			dest.writeByte((byte) 1);
			dest.writeParcelable(tips, flags);
		} else {
			dest.writeByte((byte) 0);
		}

		dest.writeString(passcode);
		dest.writeString(currency);
		dest.writeString(invoice);
		dest.writeString(note);

		if (geoLocation != null) {
			dest.writeByte((byte) 1);
			dest.writeParcelable(geoLocation, flags);
		} else {
			dest.writeByte((byte) 0);
		}
	}

	private void readFromParcel(Parcel in) {
		refData = in.readString();
		payment = (Amount) in.readParcelable(Amount.class.getClassLoader());
		tax = (in.readByte() == 1 ? (Amount) in.readParcelable(Amount.class
				.getClassLoader()) : null);
		tips = (in.readByte() == 1 ? (Amount) in.readParcelable(Amount.class
				.getClassLoader()) : null);
		passcode = in.readString();
		currency = in.readString();
		invoice = in.readString();
		note = in.readString();
		geoLocation = (in.readByte() == 1 ? (GeoLocation) in
				.readParcelable(GeoLocation.class.getClassLoader()) : null);
	}

	public static final Parcelable.Creator<ChargeParams> CREATOR = new Parcelable.Creator<ChargeParams>() {
		public ChargeParams createFromParcel(Parcel in) {
			return new ChargeParams(in);
		}

		public ChargeParams[] newArray(int size) {
			return new ChargeParams[size];
		}
	};

	private ChargeParams(Parcel in) {
		this.readFromParcel(in);
	}

}
