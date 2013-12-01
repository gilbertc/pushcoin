package com.pushcoin.lib.core.exceptions;

public class DeviceNotConstructedException extends RecoverableException {

	private static final long serialVersionUID = -6757624697158265693L;

	public DeviceNotConstructedException() {
		super("Device not constructed");
	}
}
