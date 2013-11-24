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

package com.pushcoin.lib.core.devices.nfc.acs;

import java.io.IOException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.os.AsyncTask;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.pushcoin.lib.core.payment.nfc.NfcTag;
import com.pushcoin.lib.core.utils.Logger;
import com.pushcoin.lib.core.utils.nfc.NdefReader;

// Handles ACS PICC tag using APDU commands
public class ACR122UPICCTag implements NfcTag {
	private static Logger log = Logger.getLogger(ACR122UPICCTag.class);

	public static final int PAGE_SIZE = 4;
	public static final int READ_PAGE_COUNT = 4;
	public static final int READ_PAGE_SIZE = READ_PAGE_COUNT * PAGE_SIZE;

	public static final int BUZZ_DURATION = 2; // in 100ms

	public static final int CONTROL_CODE = 3500;
	public static final byte CLS_PTS = (byte) 0xFF;
	public static final byte READ_INSTR = (byte) 0xB0;
	public static final byte WRITE_INSTR = (byte) 0xD6;
	public static final byte PSEUDO_INSTR = (byte) 0x00;
	public static final byte PSEUDO_LED_BUZZER_CONTROL = (byte) 0x40;
	public static final byte PSEUDO_TAG_DETECTED_BUZZER_CONTROL = (byte) 0x52;

	public static final byte[] LED_BUZZER_COMMAND = new byte[] { CLS_PTS,
			PSEUDO_INSTR, PSEUDO_LED_BUZZER_CONTROL, 0x50, 4, BUZZ_DURATION, 0,
			1, 1 };
	public static final byte[] TAG_DETECTED_BUZZER_COMMAND = new byte[] {
			CLS_PTS, PSEUDO_INSTR, PSEUDO_TAG_DETECTED_BUZZER_CONTROL, 0, 0 };

	byte[] readCommand = new byte[] { CLS_PTS, READ_INSTR, 0, 0, READ_PAGE_SIZE };
	byte[] writeCommand = new byte[] { CLS_PTS, WRITE_INSTR, 0, 0, PAGE_SIZE,
			0, 0, 0, 0 };

	public static void SuppressTagDetectedBuzz(Reader reader)
			throws IOException {
		byte[] initResponse = new byte[32];
		try {
			reader.control(0, ACR122UPICCTag.CONTROL_CODE,
					ACR122UPICCTag.TAG_DETECTED_BUZZER_COMMAND,
					ACR122UPICCTag.TAG_DETECTED_BUZZER_COMMAND.length,
					initResponse, initResponse.length);
		} catch (ReaderException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	Reader reader;
	int slotNum;
	byte[] id = new byte[7];
	byte[] response = new byte[32];

	public ACR122UPICCTag(Reader reader, int slotNum) {
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
				// Set buzzer
				reader.transmit(slotNum, LED_BUZZER_COMMAND,
						LED_BUZZER_COMMAND.length, response, response.length);

			} catch (ReaderException e) {
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
