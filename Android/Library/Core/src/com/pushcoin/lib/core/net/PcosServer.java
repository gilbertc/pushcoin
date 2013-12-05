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

package com.pushcoin.lib.core.net;

import com.pushcoin.lib.pcos.DocumentReader;
import com.pushcoin.lib.pcos.InputDocument;
import com.pushcoin.lib.pcos.OutputDocument;

public class PcosServer extends Server {

	

	public InputDocument post(String url, OutputDocument doc) throws Exception {
		return this.post(null, url, doc);
	}

	public InputDocument post(Object tag, String url, OutputDocument doc)
			throws Exception {
		return new DocumentReader(post(tag, url, doc.toBytes()));
	}

	public void postAsync(String url, OutputDocument doc,
			PcosServerResponseListener listener) {
		this.postAsync(null, url, doc, listener);
	}

	public void postAsync(Object tag, String url, OutputDocument doc,
			PcosServerResponseListener listener) {

		try {
			this.postAsync(tag, url, doc.toBytes(), listener);
		} catch (Exception ex) {
			listener.onError(tag, ex);
		}
	}
	
	public InputDocument stage(OutputDocument doc) throws Exception {
		return this.stage(null, doc);
	}

	public InputDocument stage(Object tag, OutputDocument doc)
			throws Exception {
		return new DocumentReader(stage(tag, doc.toBytes()));
	}

	public void stageAsync(OutputDocument doc,
			PcosServerResponseListener listener) {
		this.stageAsync(null, doc, listener);
	}

	public void stageAsync(Object tag, OutputDocument doc,
			PcosServerResponseListener listener) {

		try {
			this.stageAsync(tag, doc.toBytes(), listener);
		} catch (Exception ex) {
			listener.onError(tag, ex);
		}
	}

}
