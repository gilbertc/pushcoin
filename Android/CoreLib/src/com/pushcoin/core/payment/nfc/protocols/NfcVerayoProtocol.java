package com.pushcoin.core.payment.nfc.protocols;

import java.io.IOException;

import com.pushcoin.core.payment.nfc.NfcPayment;
import com.pushcoin.core.utils.Logger;
import com.pushcoin.core.utils.Stopwatch;
import com.pushcoin.core.data.Challenge;
import com.pushcoin.pcos.BlockWriter;
import com.pushcoin.pcos.OutputBlock;
import com.pushcoin.pcos.PcosError;

//segment tag_verayo_m1hw_50pf /* implied from protocol */
//{
//  serial_number: byte[]; /* manufacturer serial number */
//  internal := byte; /* manufacturer controlled, tagdata[9]*/
//  tag_type := byte; /* manufacturer controlled, PUF OPERATION 0xF0 */
//  challenge := bytes[]; /* PushCoin backend value */
//  response := bytes[]; /* tag's response to the challenge */
//  challenge_response_time_ms := int; /* elapsed time to obtain the challenge */
//};

public class NfcVerayoProtocol extends NfcProtocol {
	private static Logger log = Logger.getLogger(NfcVerayoProtocol.class);

	private class Response {
		byte[] response;
		int elapsed;

		// responseLength in bits
		Response(int responseLength) {
			response = new byte[responseLength / 8];
		}
	}

	private NfcPayment tech;
	private Stopwatch timer;

	public NfcVerayoProtocol(NfcPayment tech) {
		this.tech = tech;
		this.timer = new Stopwatch();
	}

	private Response doChallenge(Challenge challenge)
			throws IOException {

		Response response = new Response(256);

		// PCHelper.LogHexString("NfcVerayoProtocol-Challenge",
		// challenge.challenge);

		// Number of cycles to read the expected response
		int cycles = (int) Math.ceil((double) response.response.length
				/ tech.getReadPageSize());

		timer.start();

		// Writing Challenge
		byte[] challengeData = challenge.getChallenege();
		tech.writePage(0xD0, challengeData, 0); // Lo
		tech.writePage(0xD1, challengeData, 4); // Hi

		for (int i = 0; i < cycles; ++i) {
			// Read Response
			tech.readPages(0xC0, response.response, i * tech.getReadPageSize());

			// Send Continue Command
			if (i != cycles - 1)
				tech.writePage(0xD3, new byte[] { 0, 0x08, 0, 0 });
		}

		timer.stop();
		response.elapsed = (int) timer.duration_ms();

		// PCHelper.LogHexString("NfcVerayoProtocol-Response",
		// response.response);

		// Logger.getInstance().Log("NfcVerayoProtocol-ElapsedTime",
		// String.valueOf(response.elapsed) + "ms");
		return response;
	}

	public OutputBlock buildPcosBlock(Challenge challenge) throws IOException {

		OutputBlock ret = new BlockWriter("M1");
		try {
			ret.writeByteStr(tech.getSourceId());
			ret.writeByteStr(challenge.getAppSeed());
			ret.writeBytes(challenge.getKeyID());
			
			Response response = doChallenge(challenge);
			ret.writeByteStr(response.response);
			ret.writeUint(response.elapsed);
		} catch (PcosError e) {
			log.d("PcosError", e);
		} catch (IOException e) {
			throw e;
		}

		return ret;
	}
}