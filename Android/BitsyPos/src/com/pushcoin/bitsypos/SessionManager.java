package com.pushcoin.bitsypos;

import android.util.Log;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteQueryBuilder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.InputStream;
import java.io.IOException;

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
	public Object session(String key) {
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
		session.put( Conf.SESSION_CART, new Cart(ctx_) );

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
