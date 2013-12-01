package com.pushcoin.lib.core.devices.nfc.acs;

import java.io.IOException;

import com.acs.smartcard.CommunicationErrorException;
import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.pushcoin.lib.core.devices.DeviceManager;
import com.pushcoin.lib.core.devices.IPaymentDevice;
import com.pushcoin.lib.core.payment.PaymentListener;
import com.pushcoin.lib.core.payment.nfc.NfcPayment;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.core.utils.nfc.NdefReader;

import android.hardware.usb.UsbDevice;
import android.nfc.NdefMessage;

public class ACR122U implements IPaymentDevice, OnStateChangeListener {
	private static Logger log = Logger.getLogger(ACR122U.class);

	public static int VENDOR_ID = 1839;
	public static int PRODUCT_ID = 8704;

	public static boolean Match(UsbDevice device) {
		return device.getVendorId() == VENDOR_ID
				&& device.getProductId() == PRODUCT_ID;
	}
	public static boolean PermissionRequired() { return false; }

	public static ACR122U newInstance(DeviceManager manager) {
		return new ACR122U(manager);
	}

	private int slotNum = -1;
	private Reader reader;
	private PaymentListener receiver;

	public ACR122U(DeviceManager manager) {
		this.reader = new Reader(manager.getUsbManager());
		this.reader.setOnStateChangeListener(this);
	}

	@Override
	public void open(UsbDevice device) throws IOException {
		// Open reader
		log.d("Opening reader: " + this.getClass().getSimpleName() + " at "
				+ device.getDeviceName());

		if (reader.isSupported(device)) {
			reader.open(device);
			ACR122UPICCTag.SuppressTagDetectedBuzz(this.reader);
		}
	}

	public UsbDevice getUsbDevice() {
		return reader.getDevice();
	}

	@Override
	public void enable(PaymentListener receiver) throws IOException {
		if (isOpened()) {
			this.receiver = receiver;
			this.connect();
		} else {
			log.d("not opened");
		}
	}

	@Override
	public void disable() {
		this.receiver = null;
	}

	@Override
	public void close(UsbDevice device) {
		reader.close();
	}

	public boolean isOpened() {
		return reader.isOpened();
	}

	@Override
	public void onStateChange(int slotNum, int prevState, int currState) {
		if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
			prevState = Reader.CARD_UNKNOWN;
		}
		if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
			currState = Reader.CARD_UNKNOWN;
		}
		if (currState == Reader.CARD_PRESENT) {
			this.slotNum = slotNum;
			this.connect();
		} else {
			this.slotNum = -1;
		}
	}

	private void connect() {
		if (slotNum >= 0 && this.receiver != null) {
			try {
				this.reader.power(slotNum, Reader.CARD_WARM_RESET);
				this.reader.setProtocol(slotNum, Reader.PROTOCOL_T0
						| Reader.PROTOCOL_T1);

				ACR122UPICCTag tag = new ACR122UPICCTag(this.reader, slotNum);

				NdefMessage ndef = tag.readNdef();

				if (ndef != null) {
					byte[] payload = NdefReader.getPayload(ndef);
					if (payload != null) {
						this.receiver.onPaymentDiscovered(new NfcPayment(tag,
								payload));
					}
				}
			} catch (CommunicationErrorException e) {
				if (e.getCcidErrorCode() != 66)
					log.d("connect", e);
			} catch (Exception e) {
				log.d("connect", e);
			} finally {
				// Consume the slot
				this.slotNum = -1;
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return this.receiver != null;
	}

}
