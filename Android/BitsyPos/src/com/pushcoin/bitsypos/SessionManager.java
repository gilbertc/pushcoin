package com.pushcoin.bitsypos;

import android.content.Context;
import java.util.TreeMap;

public class SessionManager
{
	public static class Session extends TreeMap<String, Object>
	{ }

	public static SessionManager newInstance(Context ctx) 
	{
		if (inst_ == null) {
			inst_ = new SessionManager();
		}
		return inst_;
	}

	public static SessionManager getInstance() 
	{
		if (inst_ == null) {
			throw new BitsyError("Did you forget to call SessionManager.newInstance in Activity?");
		}
		return inst_;
	}

	/**
		Global scope accessor
	*/
	public Object app(String key) {
		return global_.get( key );	
	}

	/**
		Looks up an item in currently used session.
	*/
	public Object get(String key) {
		return session_.get( key );	
	}

	/**
		Initializes application-scope session
	*/
	private Session createAppSession()
	{
		return new Session();
	}

	/**
		Initializes a new interactive session.
	*/
	private Session createInteractiveSession()
	{
		Session session = new Session();

		// Put cart in session.
		session.put( Conf.SESSION_KEY_CART, new Cart() );

		return session;
	}

	/**
	 * Constructor should be private to prevent direct instantiation.
	 * make call to static factory method "getInstance()" instead.
	 */
	private SessionManager()
	{
		global_ = createAppSession();
		session_ = createInteractiveSession();
	}

	private static SessionManager inst_ = null;

	Session global_;
	Session session_;
}
