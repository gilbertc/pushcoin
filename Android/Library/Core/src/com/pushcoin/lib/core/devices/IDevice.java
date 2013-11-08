package com.pushcoin.lib.core.devices;

import java.io.IOException;

import android.hardware.usb.UsbDevice;

public interface IDevice {

	public UsbDevice getUsbDevice();

	public void open(UsbDevice device) throws IOException;

	public void close() throws IOException;

	public boolean isOpened();

}
