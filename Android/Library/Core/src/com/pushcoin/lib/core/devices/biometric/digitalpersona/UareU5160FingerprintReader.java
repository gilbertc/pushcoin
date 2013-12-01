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

package com.pushcoin.lib.core.devices.biometric.digitalpersona;

import java.io.IOException;
import android.hardware.usb.UsbDevice;

import com.pushcoin.lib.core.devices.DeviceManager;
import com.pushcoin.lib.core.devices.IQueryDevice;
import com.pushcoin.lib.core.query.QueryListener;
import com.pushcoin.lib.core.utils.Logger;

public class UareU5160FingerprintReader implements IQueryDevice {
	private static Logger log = Logger
			.getLogger(UareU5160FingerprintReader.class);

	public static int VENDOR_ID = 1466;
	public static int PRODUCT_ID = 11;

	private static UareU5160FingerprintReader instance = null;

	public static boolean Match(UsbDevice device) {
		return device.getVendorId() == VENDOR_ID
				&& device.getProductId() == PRODUCT_ID;
	}

	public static boolean PermissionRequired() {
		return false;
	}

	public static UareU5160FingerprintReader newInstance(DeviceManager manager) {
		if (instance == null)
			instance = new UareU5160FingerprintReader(manager);

		return instance;
	}

	private UareUFingerprintReaderKernel kernel;

	public UareU5160FingerprintReader(DeviceManager manager) {
		this.kernel = UareUFingerprintReaderKernel.getInstance();
	}

	@Override
	public void open(UsbDevice usbDevice) throws IOException {
		log.d("Opening reader: " + this.getClass().getSimpleName() + " at "
				+ usbDevice.getDeviceName());
	}

	@Override
	public void close(UsbDevice device) throws IOException {
	}

	@Override
	public void enable(QueryListener receiver) throws IOException {
		synchronized (this) {
			this.kernel.enable(receiver);
		}
	}

	@Override
	public void disable() {
		synchronized (this) {
			this.kernel.disable();
		}
	}

	@Override
	public boolean isEnabled() {
		synchronized (this) {
			return this.kernel.isEnabled();
		}
	}

}
