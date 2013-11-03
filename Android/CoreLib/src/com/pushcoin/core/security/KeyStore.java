package com.pushcoin.core.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import android.content.Context;

import com.pushcoin.core.utils.Logger;

public class KeyStore {

	private static final String FILE_MAT = "PushCoin.mat";
	private static final String FILE_PRIVATE_KEY = "PushCoin.private";
	private static final String FILE_PUBLIC_KEY = "PushCoin.public";

	private static Logger log = Logger.getLogger(KeyStore.class);

	private static KeyPair generateKeyPair() {

		try {
			// generate DSA keys
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

			keyGen.initialize(512, random);
			return keyGen.generateKeyPair();

		} catch (Exception ex) {
			log.e("Failed to generate keys", ex);
			return null;
		}
	}

	private static KeyStore singleton;

	public static KeyStore getInstance(Context ctxt) {
		if (singleton != null)
			return singleton;
		else
			return (singleton = new KeyStore(ctxt));
	}

	public static KeyStore getInstance() {
		return singleton;
	}

	private PrivateKey privateKey = null;
	private PublicKey publicKey = null;
	private byte[] mat = null;

	public boolean hasMAT() {
		return mat != null;
	}

	public byte[] getMAT() {
		return mat;
	}

	public void setMAT(Context ctxt, byte[] mat) {
		this.mat = mat;

		try {
			writeFile(ctxt, FILE_MAT, this.mat);
		} catch (IOException e) {
			log.e("IOException when writing mat file", e);
			this.mat = null;
		}
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void reset(Context ctxt) {

		try {
			deleteFile(ctxt, FILE_PUBLIC_KEY);
		} catch (IOException e) {
			log.e("IOException when deleting public key file", e);
		}

		try {
			deleteFile(ctxt, FILE_PRIVATE_KEY);
		} catch (IOException e) {
			log.e("IOException when deleting private key file", e);
		}

		try {
			deleteFile(ctxt, FILE_MAT);
		} catch (IOException e) {
			log.e("IOException when deleting mat file", e);
		}

		this.publicKey = null;
		this.privateKey = null;
		this.mat = null;
	}

	private KeyStore(Context ctxt) {
		byte[] publicKeyBytes = null;
		byte[] privateKeyBytes = null;
		byte[] matBytes = null;

		try {
			publicKeyBytes = readFile(ctxt, FILE_PUBLIC_KEY);
		} catch (FileNotFoundException e) {
			publicKeyBytes = null;
		} catch (IOException e) {
			log.e("IOException when opening public key file", e);
			publicKeyBytes = null;
		}

		try {
			privateKeyBytes = readFile(ctxt, FILE_PRIVATE_KEY);
		} catch (FileNotFoundException e) {
			privateKeyBytes = null;
		} catch (IOException e) {
			log.e("IOException when opening private key file", e);
			privateKeyBytes = null;
		}

		try {
			matBytes = readFile(ctxt, FILE_MAT);
		} catch (FileNotFoundException e) {
			matBytes = null;
		} catch (IOException e) {
			log.e("IOException when opening mat file", e);
			matBytes = null;
		}

		if (publicKeyBytes == null || privateKeyBytes == null) {
			createNewKeys(ctxt);
		} else {
			KeyFactory kf;
			KeySpec keySpec;
			try {
				kf = KeyFactory.getInstance("DSA");
				keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
				this.privateKey = kf.generatePrivate(keySpec);

				kf = KeyFactory.getInstance("DSA");
				keySpec = new X509EncodedKeySpec(publicKeyBytes);
				this.publicKey = kf.generatePublic(keySpec);

				this.mat = matBytes;
			} catch (Exception ex) {
				log.e("exception when parsing key files", ex);
				createNewKeys(ctxt);
			}
		}
	}

	public boolean createNewKeys(Context ctxt) {

		KeyPair keyPair = generateKeyPair();
		this.privateKey = keyPair.getPrivate();
		this.publicKey = keyPair.getPublic();
		this.mat = null;

		try {
			writeFile(ctxt, FILE_PUBLIC_KEY, this.publicKey.getEncoded());
		} catch (IOException e) {
			log.e("IOException when writing public key file", e);
			this.publicKey = null;
		}

		try {
			writeFile(ctxt, FILE_PRIVATE_KEY, this.privateKey.getEncoded());
		} catch (IOException e) {
			log.e("IOException when writing private key file", e);
			this.privateKey = null;
		}

		try {
			deleteFile(ctxt, FILE_MAT);
		} catch (IOException e) {
			log.e("IOException when deleting mat file", e);
			this.mat = null;
		}

		if (this.privateKey == null || this.publicKey == null) {
			reset(ctxt);
			return false;
		}
		return true;
	}

	private static void deleteFile(Context ctxt, String filename)
			throws IOException {
		log.d("deleting file: " + filename);
		File file = ctxt.getFileStreamPath(filename);

		if (file.exists()) {
			if (!file.delete())
				throw new IOException();
		}
	}

	private static void writeFile(Context ctxt, String filename, byte[] bytes)
			throws IOException {
		log.d("writing file: " + filename);

		FileOutputStream fout = null;
		try {
			fout = ctxt.openFileOutput(filename, Context.MODE_PRIVATE);
			fout.write(bytes);
		} finally {
			if (fout != null)
				fout.close();
		}
	}

	private static byte[] readFile(Context ctxt, String filename)
			throws IOException {
		log.d("reading file: " + filename);

		File file = ctxt.getFileStreamPath(filename);
		if (!file.exists()) {
			log.d("file not found: " + filename);
			throw new FileNotFoundException();
		}

		if (file.length() > 1024 * 1024) {
			log.e("file size failed sanity check: " + file.length());
			throw new IOException();
		}

		byte[] bytes = new byte[(int) file.length()];

		FileInputStream fin = null;

		fin = new FileInputStream(file);

		try {
			if (fin.read(bytes) != file.length()) {
				log.e("cannot read all file");
				throw new IOException();
			}
		} finally {
			if (fin != null)
				fin.close();
		}

		return bytes;
	}
}
