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

package com.pushcoin.lib.core.utils;

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
