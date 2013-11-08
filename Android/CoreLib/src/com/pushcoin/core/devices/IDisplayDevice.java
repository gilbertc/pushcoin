package com.pushcoin.core.devices;

import java.io.IOException;

import com.pushcoin.core.data.DisplayParcel;

public interface IDisplayDevice extends IDevice {
	public boolean display(DisplayParcel pkg) throws IOException;

}
