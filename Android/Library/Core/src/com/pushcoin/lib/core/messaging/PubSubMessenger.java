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
