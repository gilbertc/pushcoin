package com.pushcoin.core.exceptions;

import android.hardware.usb.UsbDevice;

public class UnsupportedDeviceException extends RecoverableException {

	private static final long serialVersionUID = -7716777350896148176L;

	public UnsupportedDeviceException(UsbDevice device) {
		super("Device " + device.getVendorId() + ":" + device.getProductId()
				+ " not supported");
	}
}