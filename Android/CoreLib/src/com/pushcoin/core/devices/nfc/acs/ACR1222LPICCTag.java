package com.pushcoin.core.devices.nfc.acs;

import java.io.IOException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.os.AsyncTask;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.pushcoin.core.data.DisplayParcel;
import com.pushcoin.core.payment.nfc.NfcTag;
import com.pushcoin.core.utils.Logger;
import com.pushcoin.core.utils.nfc.NdefReader;

public class ACR1222LPICCTag implements NfcTag {
	private static Logger log = Logger.getLogger(ACR1222LPICCTag.class);

	public static final int PAGE_SIZE = 4;
	public static final int READ_PAGE_COUNT = 4;
	public static final int READ_PAGE_SIZE = READ_PAGE_COUNT * PAGE_SIZE;

	public static final int CONTROL_CODE = 3500;
	public static final byte CLS_PTS = (byte) 0xFF;
	public static final byte READ_INSTR = (byte) 0xB0;
	public static final byte WRITE_INSTR = (byte) 0xD6;
	public static final byte PSEUDO_INSTR = (byte) 0x00;
	public static final byte LCD_DISPLAY_FONT_OPTION = (byte) 0x11;
	public static final byte PSEUDO_CLEAR_LCD_CONTROL = (byte) 0x60;
	public static final byte PSEUDO_LCD_DISPLAY_ASCII_CONTROL = (byte) 0x68;
	public static final byte PSEUDO_LCD_BACKLIGHT_CONTROL = (byte) 0x64;
	public static final byte PSEUDO_LED_CONTROL = (byte) 0x44;

	public static final byte[] CLEAR_LCD_COMMAND = new byte[] { CLS_PTS,
			PSEUDO_INSTR, PSEUDO_CLEAR_LCD_CONTROL, 0, 0 };

	public static final byte[] LCD_DISPLAY_ASCII_COMMAND = new byte[] {
			CLS_PTS, LCD_DISPLAY_FONT_OPTION, PSEUDO_LCD_DISPLAY_ASCII_CONTROL,
			0, 0 };

	public static final byte[] LCD_BACKLIGHT_COMMAND = new byte[] { CLS_PTS,
			PSEUDO_INSTR, PSEUDO_LCD_BACKLIGHT_CONTROL, 0, 0 };

	public static final byte[] SHORT_BUZZER_COMMAND = new byte[] { (byte) 0xE0,
			PSEUDO_INSTR, (byte) 0x00, (byte) 0x28, (byte) 0x01, (byte) 1 };

	public static final byte[] LONG_BUZZER_COMMAND = new byte[] { (byte) 0xE0,
			PSEUDO_INSTR, (byte) 0x00, (byte) 0x28, (byte) 0x01, (byte) 5 };

	public static final byte[] SET_DEFAULT_LEDBUZZER_COMMAND = new byte[] {
			(byte) 0xE0, PSEUDO_INSTR, (byte) 0x00, (byte) 0x21, (byte) 0x01, 0 };

	public static final byte[] LED_CONTROL_COMMAND = new byte[] { CLS_PTS,
			PSEUDO_INSTR, PSEUDO_LED_CONTROL, 0, (byte) 0x00 };

	byte[] readCommand = new byte[] { CLS_PTS, READ_INSTR, 0, 0, READ_PAGE_SIZE };
	byte[] writeCommand = new byte[] { CLS_PTS, WRITE_INSTR, 0, 0, PAGE_SIZE,
			0, 0, 0, 0 };

	public static void ShortBuzzer(Reader reader) throws IOException {
		byte[] initResponse = new byte[32];
		try {
			reader.control(0, ACR1222LPICCTag.CONTROL_CODE,
					ACR1222LPICCTag.SHORT_BUZZER_COMMAND,
					ACR1222LPICCTag.SHORT_BUZZER_COMMAND.length, initResponse,
					initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	public static void LongBuzzer(Reader reader) throws IOException {
		byte[] initResponse = new byte[32];
		try {
			reader.control(0, ACR1222LPICCTag.CONTROL_CODE,
					ACR1222LPICCTag.LONG_BUZZER_COMMAND,
					ACR1222LPICCTag.LONG_BUZZER_COMMAND.length, initResponse,
					initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	public static void SetDefaultLEDBuzzer(Reader reader, byte state)
			throws IOException {
		byte[] initResponse = new byte[32];

		byte[] req = new byte[ACR1222LPICCTag.SET_DEFAULT_LEDBUZZER_COMMAND.length];

		System.arraycopy(ACR1222LPICCTag.SET_DEFAULT_LEDBUZZER_COMMAND, 0, req,
				0, ACR1222LPICCTag.SET_DEFAULT_LEDBUZZER_COMMAND.length);

		req[5] = state;

		try {
			reader.control(0, ACR1222LPICCTag.CONTROL_CODE, req, req.length,
					initResponse, initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	public static void LEDControl(Reader reader, byte state) throws IOException {
		byte[] initResponse = new byte[32];

		byte[] req = new byte[ACR1222LPICCTag.LED_CONTROL_COMMAND.length];

		System.arraycopy(ACR1222LPICCTag.LED_CONTROL_COMMAND, 0, req, 0,
				ACR1222LPICCTag.LED_CONTROL_COMMAND.length);

		req[3] = state;

		try {
			reader.control(0, ACR1222LPICCTag.CONTROL_CODE, req, req.length,
					initResponse, initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	public static void ClearLCD(Reader reader) throws IOException {
		byte[] initResponse = new byte[32];
		try {
			reader.control(0, ACR1222LPICCTag.CONTROL_CODE,
					ACR1222LPICCTag.CLEAR_LCD_COMMAND,
					ACR1222LPICCTag.CLEAR_LCD_COMMAND.length, initResponse,
					initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	public static void LCDBacklight(Reader reader, boolean isOn)
			throws IOException {
		byte[] initResponse = new byte[32];

		byte[] req = new byte[ACR1222LPICCTag.LCD_BACKLIGHT_COMMAND.length];

		System.arraycopy(ACR1222LPICCTag.LCD_BACKLIGHT_COMMAND, 0, req, 0,
				ACR1222LPICCTag.LCD_BACKLIGHT_COMMAND.length);

		req[3] = (byte) (isOn ? 0xFF : 0x00);

		try {
			reader.control(0, ACR1222LPICCTag.CONTROL_CODE, req, req.length,
					initResponse, initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}
	
	
	public static boolean LCDDisplay(Reader reader, DisplayParcel pkg)
			throws IOException {

		if (pkg == null || pkg.getMode() == DisplayParcel.Mode.EMPTY)
			return false;

		if (pkg.getMode() != DisplayParcel.Mode.ASCII)
			return false;

		byte[] initResponse = new byte[32];

		int line = 0;
		for (String raw : pkg.getTexts()) {
			if (raw.isEmpty())
				continue;
			
			String s = DisplayParcel.FormatString(raw, pkg.getTextAlignment(), 16);
			
			byte payloadSize = (byte) Math.min(16, s.length());
			byte[] req = new byte[ACR1222LPICCTag.LCD_DISPLAY_ASCII_COMMAND.length
					+ payloadSize];

			System.arraycopy(ACR1222LPICCTag.LCD_DISPLAY_ASCII_COMMAND, 0, req,
					0, ACR1222LPICCTag.LCD_DISPLAY_ASCII_COMMAND.length);

			System.arraycopy(s.getBytes(), 0, req,
					ACR1222LPICCTag.LCD_DISPLAY_ASCII_COMMAND.length,
					payloadSize);

			req[3] = (byte) (line * 0x40);
			req[4] = payloadSize;

			try {
				reader.control(0, ACR1222LPICCTag.CONTROL_CODE, req,
						req.length, initResponse, initResponse.length);
			} catch (ReaderException ex) {
				throw new IOException(ex.getMessage());
			}

			// max 2 lines
			if (++line >= 2)
				break;
		}
		return true;
	}

	Reader reader;
	int slotNum;
	byte[] id = new byte[7];
	byte[] response = new byte[32];

	public ACR1222LPICCTag(Reader reader, int slotNum) {
		this.reader = reader;
		this.slotNum = slotNum;
	}

	@Override
	public int getPageSize() {
		return PAGE_SIZE;
	}

	@Override
	public int getReadPageSize() {
		return READ_PAGE_SIZE;
	}

	@Override
	public byte[] getId() {
		return id;
	}

	@Override
	public void setId(byte[] id) {
		this.id = id;
	}

	@Override
	public void connect() throws IOException {
	}

	private class BuzzTask extends AsyncTask<Void, Void, Void> {
		Exception ex = null;

		@Override
		protected Void doInBackground(Void... params) {

			try {
				ACR1222LPICCTag.ShortBuzzer(reader);
			} catch (Exception e) {
				this.ex = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (ex != null) {
				log.d("BuzzTask", ex);
			}
		}
	}

	@Override
	public void close() throws IOException {
		new BuzzTask().execute();
	}

	@Override
	public void writePage(int pageOffset, byte[] data, int offset)
			throws IOException {
		// Logger.getInstance().Log("ACSPICC", "writing: " + pageOffset);

		writeCommand[3] = (byte) pageOffset;
		System.arraycopy(data, offset, writeCommand, 5, PAGE_SIZE);

		try {
			reader.transmit(slotNum, writeCommand, writeCommand.length,
					response, response.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	@Override
	public void writePage(int pageOffset, byte[] data) throws IOException {
		writePage(pageOffset, data, 0);
	}

	@Override
	public int readPages(int pageOffset, byte[] dest, int offset)
			throws IOException {
		// Logger.getInstance().Log("ACSPICC", "reading: " + pageOffset);

		readCommand[3] = (byte) pageOffset;
		try {
			int length = reader.transmit(slotNum, readCommand,
					readCommand.length, response, response.length);
			int readLen = Math.min(length - 2, dest.length - offset);
			System.arraycopy(response, 0, dest, offset, readLen);
			return readLen;
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	@Override
	public byte[] readPages(int pageOffset) throws IOException {
		// Logger.getInstance().Log("ACSPICC", "reading: " + pageOffset);

		readCommand[3] = (byte) pageOffset;
		try {
			int length = reader.transmit(slotNum, readCommand,
					readCommand.length, response, response.length);
			byte[] data = new byte[length - 2];
			System.arraycopy(response, 0, data, 0, length - 2);
			return data;
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}

	}

	public NdefMessage readNdef() throws IOException, FormatException {
		return NdefReader.read(this);
	}
}