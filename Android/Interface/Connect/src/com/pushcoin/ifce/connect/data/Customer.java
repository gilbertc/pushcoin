/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
		mugshot = in.readParcelable(Bitmap.class.getClassLoader());
		balance = in.readParcelable(Amount.class.getClassLoader());
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
