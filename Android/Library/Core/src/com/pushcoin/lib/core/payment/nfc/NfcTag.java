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
