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
