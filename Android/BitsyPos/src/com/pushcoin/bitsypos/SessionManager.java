package com.pushcoin.bitsypos;

import android.content.Context;
import java.util.TreeMap;

public class SessionManager
{
	public static class Session extends TreeMap<String, Object>
	{ }

	public static SessionManager getInstance(Context ctx) 
	{
		/** 
		 * Use the application context as suggested by CommonsWare.
		 * this will ensure that you dont accidentally leak an Activitys
		 * context (see this article for more information: 
		 * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
		 */
		if (inst_ == null) {
			inst_ = new SessionManager(ctx);
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
		session.put( Conf.SESSION_KEY_CART, new Cart(ctx_) );

		return session;
	}

	/**
	 * Constructor should be private to prevent direct instantiation.
	 * make call to static factory method "getInstance()" instead.
	 */
	private SessionManager(Context ctx) 
	{
		// Activity context
		ctx_ = ctx;

		global_ = createAppSession();
		session_ = createInteractiveSession();
	}

	private Context ctx_;
	private static SessionManager inst_ = null;

	Session global_;
	Session session_;
}
