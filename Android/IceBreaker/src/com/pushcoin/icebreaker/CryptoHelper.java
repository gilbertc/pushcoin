package com.pushcoin.icebreaker;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;

public class CryptoHelper
{
	static public class DsaKeyPair
	{
		PrivateKey privateKey;
		PublicKey publicKey;
	}

	static public DsaKeyPair generateDsaKeyPair() 
	{
		try 
		{
			// Get the DSA key factory
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(Conf.DSA_KEY_LENGTH);
			KeyPair keypair = keyGen.genKeyPair();

			DsaKeyPair pair = new DsaKeyPair();

			// Get the private key
			pair.privateKey = keypair.getPrivate();
			pair.publicKey = keypair.getPublic();
			return pair;
		}
		catch( NoSuchAlgorithmException e ) {
			throw new RuntimeException( e );
		}
	}
}

