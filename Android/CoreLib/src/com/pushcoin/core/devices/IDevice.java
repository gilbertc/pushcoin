package com.pushcoin.core.devices;

import java.io.IOException;

import com.pushcoin.core.messaging.MessageReceiver;

import android.hardware.usb.UsbDevice;

public interface IDevice extends MessageReceiver{
	
	public UsbDevice getUsbDevice();
	public void open(UsbDevice device) throws IOException;
	public void close() throws IOException;
	public boolean isOpened();
	
	
}
