package com.pushcoin.ifce.connect.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigDecimal;

public class Amount implements Parcelable {

	public long value = 0;
	public int scale = 0;

	public Amount(long value, int scale) {
		this.value = value;
		this.scale = scale;
	}

	public Amount(BigDecimal v) {
		this.value = v.longValue();
		this.scale = v.scale();
	}

	public BigDecimal asDecimal() {
		return BigDecimal.valueOf(value, -scale);
	}

	/**
	 * Parcelable logic below.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(value);
		dest.writeInt(scale);
	}

	private void readFromParcel(Parcel in) {
		value = in.readLong();
		scale = in.readInt();
	}

	public static final Parcelable.Creator<Amount> CREATOR = new Parcelable.Creator<Amount>() {
		public Amount createFromParcel(Parcel in) {
			return new Amount(in);
		}

		public Amount[] newArray(int size) {
			return new Amount[size];
		}
	};

	private Amount(Parcel in) {
		this.readFromParcel(in);
	}
}
