package com.pushcoin.lib.core.devices;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import com.pushcoin.lib.core.data.DisplayParcel;
import com.pushcoin.lib.core.devices.magnetic.magtek.MiniReader;
import com.pushcoin.lib.core.devices.nfc.acs.ACR1222L;
import com.pushcoin.lib.core.devices.nfc.acs.ACR122U;
import com.pushcoin.lib.core.payment.PaymentListener;
import com.pushcoin.lib.core.utils.Logger;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.SparseArray;

public class DeviceManager {
	private static Logger log = Logger.getLogger(DeviceManager.class);
	public static final String ACTION_USB_PERMISSION = "com.pushcoin.lib.core.devices.USB_PERMISSION";

	private static DeviceManager _defaultDeviceManager;

	public static DeviceManager getDefault() {
		return _defaultDeviceManager;
	}

	public static DeviceManager createDefault(Context context,
			UsbManager usbManager) {
		if (_defaultDeviceManager == null) {
			_defaultDeviceManager = new DeviceManager(context, usbManager);
		}
		return _defaultDeviceManager;
	}

	private class PendingPermissionRequest {
		public Context context;
		public UsbDevice usbDevice;
	}

	private SparseArray<IDevice> devices;
	private PaymentListener dispatchListener;
	private UsbManager usbManager;
	private DisplayParcel displayParcel;

	private boolean isPermissionRequesting = false;
	private Queue<PendingPermissionRequest> pendingRequests = new ArrayDeque<PendingPermissionRequest>();

	public DeviceManager(Context context, UsbManager usbManager) {
		log.d("creating device manager");
		this.usbManager = usbManager;
		this.devices = new SparseArray<IDevice>();

		for (UsbDevice usbDevice : this.usbManager.getDeviceList().values()) {
			log.d("at-constr: " + usbDevice.getVendorId() + ":"
					+ usbDevice.getProductId());
			requestPermission(context, usbDevice);
		}
	}

	public void requestPermission(Context context, UsbDevice usbDevice) {
		if (this.isPermissionRequesting) {
			PendingPermissionRequest r = new PendingPermissionRequest();
			r.context = context;
			r.usbDevice = usbDevice;

			this.pendingRequests.add(r);
		} else {
			this.isPermissionRequesting = true;
			PendingIntent permissionIntent = PendingIntent.getBroadcast(
					context, 0, new Intent(ACTION_USB_PERMISSION), 0);

			this.usbManager.requestPermission(usbDevice, permissionIntent);
		}
	}

	public void onPermissionGranted(Context context, UsbDevice usbDevice) {
		this.isPermissionRequesting = false;
		this.onDeviceAttached(usbDevice);

		// Ignore attached devices
		while (!this.pendingRequests.isEmpty()) {
			PendingPermissionRequest r = this.pendingRequests.remove();
			if (this.devices.indexOfKey(r.usbDevice.getDeviceId()) < 0) {
				this.requestPermission(r.context, r.usbDevice);
				break;
			}
		}
	}

	public UsbManager getUsbManager() {
		return usbManager;
	}

	public boolean isSupported(UsbDevice usbDevice) {
		return getDeviceClass(usbDevice) != null;
	}

	public Class<? extends IDevice> getDeviceClass(UsbDevice usbDevice) {
		if (ACR1222L.Match(usbDevice))
			return ACR1222L.class;
		if (ACR122U.Match(usbDevice))
			return ACR122U.class;
		if (MiniReader.Match(usbDevice))
			return MiniReader.class;
		return null;
	}

	public void onDeviceAttached(UsbDevice usbDevice) {
		Class<? extends IDevice> deviceClass = getDeviceClass(usbDevice);
		if (deviceClass != null) {
			IDevice device = openDevice(deviceClass, usbDevice);
			if (device != null) {
				devices.put(usbDevice.getDeviceId(), device);
			}
		}
	}

	public void onDeviceDetached(UsbDevice usbDevice) {
		try {
			IDevice device = devices.get(usbDevice.getDeviceId());
			if (device != null) {
				closeDevice(device);
				devices.remove(usbDevice.getDeviceId());
			}
		} catch (Exception ex) {
			log.e("closeDevice", ex);
		}
	}

	private IDevice openDevice(Class<? extends IDevice> deviceClass,
			UsbDevice usbDevice) {
		log.d("open device");
		try {
			IDevice device = (IDevice) deviceClass.getConstructor(
					DeviceManager.class).newInstance(this);

			device.open(usbDevice);

			if (this.dispatchListener != null
					&& device instanceof IPaymentDevice) {
				((IPaymentDevice) device).enable(this.dispatchListener);
			}

			if (this.displayParcel != null && device instanceof IDisplayDevice) {
				((IDisplayDevice) device).display(this.displayParcel);
			}

			return device;
		} catch (Exception e) {
			log.d("message", e);
		}
		return null;
	}

	private void closeDevice(IDevice device) throws IOException {
		device.close();
	}

	public void enable(PaymentListener receiver) throws IOException {
		log.d("enable");
		this.dispatchListener = receiver;

		for (int i = 0; i < this.devices.size(); ++i) {
			log.d("trying device");
			IDevice device = this.devices.valueAt(i);
			if (device instanceof IPaymentDevice) {
				((IPaymentDevice) device).enable(this.dispatchListener);
			}
		}
	}

	public void disable() {
		log.d("disable");
		this.dispatchListener = null;

		for (int i = 0; i < this.devices.size(); ++i) {
			IDevice device = this.devices.valueAt(i);
			if (device instanceof IPaymentDevice)
				((IPaymentDevice) device).disable();
		}
	}

	public void display(DisplayParcel parcel) throws IOException {
		log.d("display");
		this.displayParcel = parcel;
		for (int i = 0; i < this.devices.size(); ++i) {
			IDevice device = this.devices.valueAt(i);
			if (device instanceof IDisplayDevice)
				((IDisplayDevice) device).display(parcel);
		}
	}

	public void cancel() {
		disable();
	}
}
