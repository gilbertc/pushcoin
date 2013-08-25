// Copyright (c) 2012 PushCoin, Inc.
//
// GNU General Public Licence (GPL)
// 
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
// details.
// You should have received a copy of the GNU General Public License along with
// this program; if not, write to the Free Software Foundation, Inc., 59 Temple
// Place, Suite 330, Boston, MA  02111-1307  USA
//
// __author__  = '''Slawomir Lisznianski <sl@pushcoin.com>'''

package com.pushcoin;

import java.nio.ByteBuffer;

public class Binascii
{
	private static final char charGlyph_[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	public static String hexlify(byte[] bytes)
	{
		StringBuilder hexAscii = new StringBuilder(bytes.length * 2);

		for (int i=0; i < bytes.length; ++i) 
		{
			byte b = bytes[i];
			hexAscii.append( charGlyph_[ (int)(b & 0xf0) >> 4] );
			hexAscii.append( charGlyph_[ (int)(b & 0x0f)] );
		}      
		return hexAscii.toString();
	}

	public static byte[] unhexlify(String asciiHex)
	{
		if(asciiHex.length()%2 != 0) {
			throw new RuntimeException( "Input to unhexlify must have even-length");
		}

    int len = asciiHex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) 
		{
	    data[i / 2] = (byte) ((Character.digit(asciiHex.charAt(i), 16) << 4) + 
				Character.digit(asciiHex.charAt(i+1), 16));
    }
    return data;
	}
}
