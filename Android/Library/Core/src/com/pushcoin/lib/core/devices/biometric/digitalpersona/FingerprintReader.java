package com.pushcoin.lib.core.devices.biometric.digitalpersona;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.pushcoin.lib.core.devices.DeviceManager;
import com.pushcoin.lib.core.devices.IPaymentDevice;
import com.pushcoin.lib.core.payment.PaymentListener;
import com.pushcoin.lib.core.payment.biometric.FingerprintPayment;
import com.pushcoin.lib.core.utils.Logger;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.Reader.Priority;

public class FingerprintReader implements IPaymentDevice {
	private static Logger log = Logger.getLogger(FingerprintReader.class);

	public static int VENDOR_ID = 1466;
	public static int PRODUCT_ID = 11;

	private UsbManager usbManager;
	private UsbDevice usbDevice;
	private PaymentListener receiver;
	private Reader reader;

	private volatile boolean isRunning = false;

	public static boolean Match(UsbDevice device) {
		return device.getVendorId() == VENDOR_ID
				&& device.getProductId() == PRODUCT_ID;
	}

	public FingerprintReader(DeviceManager manager) {
		this.usbManager = manager.getUsbManager();
	}

	@Override
	public UsbDevice getUsbDevice() {
		return usbDevice;
	}

	@Override
	public void open(UsbDevice usbDevice) throws IOException {
		log.d("Opening reader: " + this.getClass().getSimpleName() + " at "
				+ usbDevice.getDeviceName());

		this.usbDevice = usbDevice;
		
		ReaderCollection readers = UareUGlobal.GetReaderCollection();
		
		if (readers.size() == 0) {
			return;
		}
		
		Reader r = readers.get(0);
		if (r.GetDescription().serial_number == reader.GetDescription().serial_number) {
			return;
		}

		reader = r;
		disableCamera();
		
		reader.Open(Priority.EXCLUSIVE);
		

			class ReadTask implements Runnable {
				private UsbEndpoint usbEndpoint;
				private UsbDeviceConnection usbDeviceConnection;
				private UsbInterface usbInterface;

				@Override
				public void run() {
					log.d("run-thread-started");

					if (!usbDeviceConnection.claimInterface(usbInterface, true)) {
						log.d("cannot-claim-interface: "
								+ usbInterface.toString());
						return;
					}

					byte[] buf = new byte[1024];

					isRunning = true;
					while (isRunning) {
						int readLen = usbDeviceConnection.bulkTransfer(
								usbEndpoint, buf, 1024, 1000);
						if (readLen > 0) {
							synchronized (FingerprintReader.this) {
								if (receiver != null) {
									receiver.onPaymentDiscovered(new FingerprintPayment(
											buf, readLen));
								}
							}
							buf = new byte[1024];
						}
					}

					usbDeviceConnection.releaseInterface(usbInterface);
					usbDeviceConnection.close();
					log.d("run-thread-ended");
				}
			}

			ReadTask task = new ReadTask();
			task.usbDeviceConnection = usbDeviceConnection;
			task.usbEndpoint = usbEndpoint;
			task.usbInterface = usbInterface;

			Thread run = new Thread(task);
			run.start();
		}
	}

	@Override
	public void close() throws IOException {
		this.isRunning = false;
		this.reader.Close();
		this.reader = null;
		
		enableCamera();
	}

	@Override
	public boolean isOpened() {
		return this.isRunning && this.readerSerialNumber != "";
	}

	@Override
	public void enable(PaymentListener receiver) throws IOException {
		synchronized (this) {
			this.receiver = receiver;
		}
	}

	@Override
	public void disable() {
		synchronized (this) {
			this.receiver = null;
		}
	}

	@Override
	public boolean isEnabled() {
		synchronized (this) {
			return this.receiver != null;
		}
	}

	private Camera m_Camera = null;

	// re-enable camera on exit
	public void enableCamera() {
		try {
			if (m_Camera != null) {
				m_Camera.release();
				m_Camera = null;
			}
		} catch (Exception e) {
		}
	}

	// prevents gallery app from popping up whenever a finger is detected by the
	// fingerprint reader
	private void disableCamera() {
		try {
			if (m_Camera == null) {
				for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
					CameraInfo camInfo = new CameraInfo();
					Camera.getCameraInfo(camNo, camInfo);

					if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT)) {
						m_Camera = Camera.open(camNo);
					}
				}
				if (m_Camera == null) {
					m_Camera = Camera.open();
				}
			}
		} catch (Exception e) {

		}
	}

}
