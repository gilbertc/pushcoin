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

package com.pushcoin.lib.core.payment.nfc;

import java.io.IOException;

import com.pushcoin.lib.core.data.PcosMsgId;
import com.pushcoin.lib.core.payment.IPayment;
import com.pushcoin.lib.core.payment.nfc.protocols.NfcProtocol;
import com.pushcoin.lib.core.payment.nfc.protocols.NfcVerayoProtocol;
import com.pushcoin.lib.core.utils.Logger;

import com.pushcoin.lib.pcos.DocumentReader;
import com.pushcoin.lib.pcos.DocumentWriter;
import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputDocument;
import com.pushcoin.lib.pcos.PcosError;
import com.pushcoin.lib.pcos.ProtocolTag;

import com.pushcoin.lib.core.data.IChallenge;
import com.pushcoin.lib.core.exceptions.InvalidProtocolException;

//segment device_meta
//{
//tag_protocol: int; /* NTAG206, M1HW, Active/PTA */
//}

public class NfcPayment implements IPayment {
	private static Logger log = Logger.getLogger(NfcPayment.class);

	byte[] sourceId;
	int protocolId;

	private NfcTag tag;
	private NfcProtocol protocol;
	private NfcPayload payload;

	public NfcPayment(NfcTag tag, byte[] payload)
			throws InvalidProtocolException, PcosError {
		this.tag = tag;
		if (payload != null)
			parse(new DocumentReader(payload));
	}

	public void parse(InputDocument doc) throws PcosError,
			InvalidProtocolException {
		if (doc.getMagic().compareTo(new String(ProtocolTag.PROTOCOL_MAGIC)) != 0)
			throw new InvalidProtocolException("Invalid PCOS Magic: "
					+ doc.getMagic());

		if (doc.getDocumentName().compareTo("Td") != 0)
			throw new InvalidProtocolException("Invalid Doc Received: "
					+ doc.getDocumentName());

		sourceId = tag.getId();

		InputBlock protoBlk = doc.getBlock("Dm");
		protocolId = (int) protoBlk.readUint();

		switch (protocolId) {
		case NfcProtocol.PROTO_VERAYO_M1HW_50PF:
			this.protocol = new NfcVerayoProtocol(this);
			break;
		default:
			log.d("Protocol Id not supported: " + protocolId);
			break;
		}

		payload = new NfcPayload(doc);
	}

	public void connect() throws IOException {
		tag.connect();
	}

	public byte[] getSourceId() {
		return sourceId;
	}

	public NfcProtocol getProtocol() {
		return protocol;
	}

	public NfcPayload getPayload() {
		return payload;
	}

	public int getPageSize() {
		return tag.getPageSize();
	}

	public int getReadPageSize() {
		return tag.getReadPageSize();
	}

	public void writePage(int pageOffset, byte[] data, int offset)
			throws IOException {
		tag.writePage(pageOffset, data, offset);
	}

	public void writePage(int pageOffset, byte[] data) throws IOException {
		tag.writePage(pageOffset, data);
	}

	public int readPages(int pageOffset, byte[] dest, int offset)
			throws IOException {
		return tag.readPages(pageOffset, dest, offset);
	}

	public byte[] readPages(int pageOffset) throws IOException {
		return tag.readPages(pageOffset);
	}

	public byte[] getMessage(IChallenge challenge) throws Exception {

		if (challenge == null)
			throw new Exception("Challenge not ready");

		OutputDocument doc = new DocumentWriter(PcosMsgId.PaymentKey);

		doc.addBlock(getPayload().buildPcosBlock(challenge));
		doc.addBlock(getProtocol().buildPcosBlock(challenge));

		return doc.toBytes();
	}

	public void close() throws IOException {
		tag.close();
	}
}
