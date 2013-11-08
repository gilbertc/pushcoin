package com.pushcoin.core.payment.nfc;

import java.io.IOException;

import com.pushcoin.core.data.PcosMsgId;
import com.pushcoin.core.payment.IPayment;
import com.pushcoin.core.payment.nfc.protocols.NfcProtocol;
import com.pushcoin.core.payment.nfc.protocols.NfcVerayoProtocol;
import com.pushcoin.core.utils.Logger;

import com.pushcoin.pcos.DocumentReader;
import com.pushcoin.pcos.DocumentWriter;
import com.pushcoin.pcos.InputBlock;
import com.pushcoin.pcos.InputDocument;
import com.pushcoin.pcos.OutputDocument;
import com.pushcoin.pcos.PcosError;
import com.pushcoin.pcos.ProtocolTag;

import com.pushcoin.core.data.Challenge;
import com.pushcoin.core.exceptions.InvalidProtocolException;

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

	public byte[] getMessage(Challenge challenge) throws Exception {

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