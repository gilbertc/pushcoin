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

import com.pushcoin.Binascii;

public final class TestDriver 
{
	static final byte[] rawbytes = {0xa, 0x2, (byte) 0xff};
	static final String varstr = "variable string";
	
	/**
	 * @param args
	 */
	public static void main( String[] args ) 
	{
		try 
		{
			System.out.println( Binascii.hexlify( rawbytes ) );
/*
			final byte[] input = testWriteDatatypes();
			StringBuilder sb = new StringBuilder();
			for (byte b : input) 
			{
				sb.append(String.format("%02X ", b));
			}
			System.out.println("size=" + input.length + ", val=" + sb.toString());
			testReadDatatypes( input );

			System.out.println( "All checks out!" );
*/
			System.exit(0);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println( "Basic error: " + e );
		}
	}
}
