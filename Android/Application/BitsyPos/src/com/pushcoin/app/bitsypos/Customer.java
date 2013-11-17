package com.pushcoin.app.bitsypos;

import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigDecimal;
import android.graphics.Bitmap;

public class Customer implements Parcelable
{
	String accountId;
	String firstName;	
	String lastName;
	String title;
	String identifier;
	Bitmap mugshot;
	BigDecimal balance;

	public static Customer newInstance() {
		return new Customer();
	}

	private Customer() {
	}

	/**
		Parcelable support below this point.

		Make sure to update code below when you add/remove 
		any class-member variables.
	*/
	private Customer( Parcel in )
	{
		accountId = in.readString();
		firstName = in.readString();
		lastName = in.readString();
		title = in.readString();
		identifier = in.readString();
		mugshot = in.readParcelable(null);
		balance = new BigDecimal( in.readString() );
	}

	@Override
	public void writeToParcel( Parcel out, int flags )
	{
		out.writeString( accountId );
		out.writeString( firstName );
		out.writeString( lastName );
		out.writeString( title );
		out.writeString( identifier );
		out.writeParcelable( mugshot, flags );
		out.writeString( balance.toString() );
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Customer> CREATOR = 
		new Parcelable.Creator<Customer>()
		{
			public Customer createFromParcel( Parcel in ) {
				return new Customer( in );
			}

			public Customer[] newArray( int size ) {
				return new Customer[size];
			}
		};
}
