package com.pushcoin.lib.core.devices;

import java.io.IOException;

import com.pushcoin.lib.core.data.DisplayParcel;

public interface IDisplayDevice extends IDevice {
	public boolean display(DisplayParcel pkg) throws IOException;

}
