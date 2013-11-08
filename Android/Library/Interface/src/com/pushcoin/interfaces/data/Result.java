package com.pushcoin.interfaces.data;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

public class Result implements Parcelable {
	
	public static final int RESULT_OK = Activity.RESULT_OK;
	public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
	
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_BOOTSTRAP = 1;
	public static final int TYPE_CHARGE = 2;
	public static final int TYPE_REGISTER = 3;
	public static final int TYPE_SETTINGS = 4;
		
	public int result = RESULT_OK;
	public int type = TYPE_UNKNOWN;
	public String reason = "OK";

	public Result()
	{
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(result);
		dest.writeInt(type);
		dest.writeString(reason);
	}
	
	private void readFromParcel(Parcel in) {
		result = in.readInt();
		type = in.readInt();
        reason = in.readString();
	}

    public static final Parcelable.Creator<Result> CREATOR
            = new Parcelable.Creator<Result>() {
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
    
    private Result(Parcel in) {
    	this.readFromParcel(in);
    }
}
