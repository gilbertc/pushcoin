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

package com.pushcoin.lib.core.utils.nfc;

import java.io.IOException;

import com.pushcoin.lib.core.payment.nfc.NfcTag;
import com.pushcoin.lib.core.utils.Logger;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class NdefReader {
	private static Logger log = Logger.getLogger(NdefReader.class);

	public static byte NDEF_MAGIC = (byte) 0xE1;
	public static byte NDEF_VERSION = (byte) 0x10;

	public static byte TLV_NDEF_MAGIC = 0x03;

	public static byte[] getPayload(NfcTag tag) throws IOException,
			FormatException {
		return getPayload(read(tag));
	}

	public static byte[] getPayload(NdefMessage msg) {
		// Find the Pcos record
		NdefRecord[] records = msg.getRecords();
		for (NdefRecord record : records) {
			if (record.getTnf() != NdefRecord.TNF_EXTERNAL_TYPE)
				continue;

			if (new String(record.getType()).compareTo("pushcoin.com:pcos") != 0)
				continue;

			return record.getPayload();
		}
		return null;
	}

	public static NdefMessage read(NfcTag tag) throws IOException,
			FormatException {

		int page = 0;
		int readPage = tag.getReadPageSize() / tag.getPageSize();

		// Read first header page
		byte[] header = tag.readPages(page);
		if (header.length != tag.getReadPageSize()) {
			log.d("Read Failed");
			return null;
		}

		// Check capability container
		if (header[3 * tag.getPageSize() + 0] != NDEF_MAGIC) {
			log.d("Invalid NDEF MAGIC");
			return null;
		}

		// Get Id
		byte[] id = new byte[7];
		System.arraycopy(header, 0, id, 0, 3);
		System.arraycopy(header, 4, id, 3, 4);
		tag.setId(id);

		// Read Data page
		page += readPage;
		byte[] msgFrag = tag.readPages(page);
		if (msgFrag.length != tag.getReadPageSize()) {
			log.d("Read Failed");
			return null;
		}

		// Check TLV
		if (msgFrag[0 * tag.getPageSize() + 0] != TLV_NDEF_MAGIC) {
			log.d("Invalid NDEF MAGIC");
			return null;
		}

		int length = (int) msgFrag[0 * tag.getPageSize() + 1];
		int remainBytes = Math.max(0, length - (msgFrag.length - 2));

		byte[] ret = new byte[length];
		System.arraycopy(msgFrag, 2, ret, 0, length - remainBytes);

		int readLen;
		while (remainBytes > 0) {
			page += readPage;
			readLen = tag.readPages(page, ret, length - remainBytes);
			if (readLen != Math.min(tag.getReadPageSize(), remainBytes)) {
				log.d("Read Failed");
				return null;
			}
			remainBytes -= readLen;
		}
		return new NdefMessage(ret);
	}
}
