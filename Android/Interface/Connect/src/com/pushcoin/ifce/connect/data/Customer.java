package com.pushcoin.ifce.connect.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Customer implements Parcelable {

	public String accountId;
	public String firstName;
	public String lastName;
	public String title;
	public String identifier;
	public Bitmap mugshot;
	public Amount balance;

	public Customer() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(accountId);
		dest.writeString(firstName);
		dest.writeString(lastName);
		dest.writeString(title);
		dest.writeString(identifier);
		dest.writeParcelable(mugshot, flags);
		dest.writeParcelable(balance, flags);
	}

	private void readFromParcel(Parcel in) {
		accountId = in.readString();
		firstName = in.readString();
		lastName = in.readString();
		title = in.readString();
		identifier = in.readString();
		in.readParcelable(Bitmap.class.getClassLoader());
		in.readParcelable(Amount.class.getClassLoader());
	}

	public static final Parcelable.Creator<Customer> CREATOR = new Parcelable.Creator<Customer>() {
		public Customer createFromParcel(Parcel in) {
			return new Customer(in);
		}

		public Customer[] newArray(int size) {
			return new Customer[size];
		}
	};

	private Customer(Parcel in) {
		this.readFromParcel(in);
	}
}
