package com.pushcoin.interfaces.data;

import android.os.Parcel;
import android.os.Parcelable;

public class GeoLocation implements Parcelable {

	public double latitude = 0.0;
	public double longitude = 0.0;
	
	public GeoLocation()
	{
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
	}
	
	private void readFromParcel(Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
	}

    public static final Parcelable.Creator<GeoLocation> CREATOR
            = new Parcelable.Creator<GeoLocation>() {
        public GeoLocation createFromParcel(Parcel in) {
            return new GeoLocation(in);
        }

        public GeoLocation[] newArray(int size) {
            return new GeoLocation[size];
        }
    };
    
    private GeoLocation(Parcel in) {
    	this.readFromParcel(in);
    }
}
