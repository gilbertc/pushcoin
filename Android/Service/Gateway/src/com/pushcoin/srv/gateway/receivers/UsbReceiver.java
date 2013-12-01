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

package com.pushcoin.srv.gateway.receivers;

import com.pushcoin.lib.core.devices.DeviceManager;
import com.pushcoin.lib.core.utils.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class UsbReceiver extends BroadcastReceiver {
	private static Logger log = Logger.getLogger(UsbReceiver.class);

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
			log.d("Action: " + action);
			UsbDevice usbDevice = (UsbDevice) intent
					.getParcelableExtra(UsbManager.EXTRA_DEVICE);

			log.d("Device: " + usbDevice.getVendorId() + ":"
					+ usbDevice.getProductId());

			UsbManager usbManager = (UsbManager) context
					.getSystemService(Context.USB_SERVICE);

			DeviceManager deviceManager = DeviceManager.createDefault(context,
					usbManager);

			if (deviceManager != null && deviceManager.isSupported(usbDevice)) {
				log.d("at-usbreceiver: " + usbDevice.getVendorId() + ":"
						+ usbDevice.getProductId());
				deviceManager.requestPermission(context, usbDevice);
			}

		} else if (DeviceManager.ACTION_USB_PERMISSION.equals(action)) {
			log.d("Action: " + action);
			UsbDevice usbDevice = (UsbDevice) intent
					.getParcelableExtra(UsbManager.EXTRA_DEVICE);

			if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
					false)) {
				UsbManager usbManager = (UsbManager) context
						.getSystemService(Context.USB_SERVICE);

				DeviceManager deviceManager = DeviceManager.createDefault(
						context, usbManager);

				if (deviceManager != null)
					deviceManager.onPermissionGranted(context, usbDevice);
			}

		} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
			log.d("Action: " + action);
			UsbDevice usbDevice = (UsbDevice) intent
					.getParcelableExtra(UsbManager.EXTRA_DEVICE);

			UsbManager usbManager = (UsbManager) context
					.getSystemService(Context.USB_SERVICE);

			DeviceManager deviceManager = DeviceManager.createDefault(context,
					usbManager);

			if (deviceManager != null)
				deviceManager.onDeviceDetached(usbDevice);
		}
	}

}
