package com.pushcoin.app.bitsypos;

import android.util.Log;
import android.content.Context;
import android.os.Message;
import android.os.Handler;
import java.util.HashSet;
import java.util.Set;

public class EventHub
{
	public static EventHub newInstance(Context ctx) 
	{
		if (inst_ == null) {
			inst_ = new EventHub();
		}
		return inst_;
	}

	public static EventHub getInstance() 
	{
		if (inst_ == null) {
			throw new BitsyError("Did you forget to call EventHub.newInstance in Activity?");
		}
		return inst_;
	}

	/**
		Registers an event consumer.
	*/
	void register( Handler handler, String name )
	{
		Log.v(Conf.TAG, "event-hub-add-handler|name="+name );
		synchronized(lock_) {
			handlers_.add( handler );
		}
	}

	/**
		Removes an event consumer.
	*/
	void unregister( Handler handler )
	{
		Log.v(Conf.TAG, "event-hub-remove-handler");
		synchronized(lock_) {
			handlers_.remove( handler );
		}
	}

	/**
		Sends a message to all registered consumers.
	*/
	public void post( Message m )
	{
		synchronized(lock_)
		{
			for (Handler rcv: handlers_)
			{
				Message nm = Message.obtain( m );
				nm.setTarget( rcv );
				nm.sendToTarget();
			}
		}
	}

	/**
		Helpers to quickly send a message.
	*/
	public static void post( int what )
	{
		Message m = Message.obtain();
		m.what = what;
		getInstance().post( m );
	}

	public static void post( int what, Object obj )
	{
		Message m = Message.obtain();
		m.what = what;
		m.obj = obj;
		getInstance().post( m );
	}

	/**
	 * Constructor should be private to prevent direct instantiation.
	 */
	private EventHub() {
	}

	private Set<Handler> handlers_ = new HashSet<Handler>();
	private Object lock_ = new Object();
	private static EventHub inst_ = null;
}
