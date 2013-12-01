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

package com.pushcoin.lib.core.messaging;

import java.util.HashSet;
import java.util.Set;

import android.os.Message;
import android.util.SparseArray;

public class PubSubMessenger {
	private SparseArray<Set<MessageReceiver>> subscriptions;

	public void register(int msgType, MessageReceiver receiver) {
		Set<MessageReceiver> sub = subscriptions.get(msgType);
		if (sub == null) {
			sub = new HashSet<MessageReceiver>();
			subscriptions.put(msgType, sub);
		}
		sub.add(receiver);
	}

	public void unregister(Integer msgType, MessageReceiver receiver) {
		Set<MessageReceiver> sub = subscriptions.get(msgType);
		if (sub != null) {
			sub.remove(receiver);
			if (sub.isEmpty())
				subscriptions.remove(msgType);
		}
	}
	
	public boolean publish(Message msg)
	{
		boolean ret = false;
		Set<MessageReceiver> sub = subscriptions.get(msg.what);
		if (sub != null) {
			for (MessageReceiver receiver : sub) {
				ret |= receiver.handleMessage(msg);
			}
		}
		return ret;
	}
}
