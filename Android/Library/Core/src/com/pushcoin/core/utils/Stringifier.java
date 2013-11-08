package com.pushcoin.core.utils;

public class Stringifier {
	static final private java.lang.String HEXES = "0123456789ABCDEF";
	
	static public java.lang.String toString(byte[] data)
	{
		if(data == null) return null;
		
		StringBuilder sb = new StringBuilder();
		for ( final byte b : data ) {
		      sb.append(HEXES.charAt((b & 0xF0) >> 4))
		         .append(HEXES.charAt((b & 0x0F)));
		}
		return sb.toString();
	}
}
