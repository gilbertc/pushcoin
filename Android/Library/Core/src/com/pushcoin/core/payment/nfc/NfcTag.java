package com.pushcoin.core.payment.nfc;

import java.io.IOException;


public interface NfcTag {

	public void connect() throws IOException;

	public void close() throws IOException;

	public byte[] getId();
	public void setId(byte[] id);

	public void writePage(int pageOffset, byte[] data) throws IOException;

	public void writePage(int pageOffset, byte[] data, int offset) throws IOException;
	
	public byte[] readPages(int pageOffset) throws IOException;
	
	public int readPages(int pageOffset, byte[] dest, int offset) throws IOException;
	
	public int getPageSize();
	public int getReadPageSize();
	
}