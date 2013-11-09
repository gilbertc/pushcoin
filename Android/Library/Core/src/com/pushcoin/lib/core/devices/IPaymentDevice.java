package com.pushcoin.lib.core.devices;

import java.io.IOException;

import com.pushcoin.lib.core.payment.PaymentListener;

public interface IPaymentDevice extends IDevice {
	public void enable(PaymentListener receiver) throws IOException;

	public void disable();

	public boolean isEnabled();
}