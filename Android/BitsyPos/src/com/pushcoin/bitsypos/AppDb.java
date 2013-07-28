package com.pushcoin.bitsypos;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;

public class AppDb extends SQLiteAssetHelper 
{
	/**
		Get items matching a given tag.
	*/
	public ArrayList<Item> findItems( String tag ) 
	{ 
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery( Conf.SQL_FIND_ITEM_BY_TAG, new String[]{tag} );

		ArrayList<Item> rs = new ArrayList<Item>();

		if ( !c.moveToFirst() ) {
			return rs;
		}

		do 
		{
			rs.add( new Item(this, c.getString(0), c.getString(1)) );
			Log.v(Conf.TAG, "find-items|tag="+tag+";item_id="+c.getString(0) + ";name="+c.getString(1) );
		}
		while (c.moveToNext());

		return rs;
	}

	public static AppDb getInstance(Context ctx) 
	{
		/** 
		 * Use the application context as suggested by CommonsWare.
		 * this will ensure that you dont accidentally leak an Activitys
		 * context (see this article for more information: 
		 * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
		 */
		if (inst_ == null) {
			inst_ = new AppDb(ctx.getApplicationContext());
		}
		return inst_;
	}

	/**
	 * Constructor should be private to prevent direct instantiation.
	 * make call to static factory method "getInstance()" instead.
	 */
	private AppDb(Context ctx) 
	{
		super(ctx, Conf.DATABASE_NAME, null, Conf.DATABASE_VERSION);
		// you can use an alternate constructor to specify a database location
		// (such as a folder on the sd card)
		// you must ensure that this folder is available and you have permission
		// to write to it
		//super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
	
		ctx_ = ctx;
	}

	private Context ctx_;
	private static AppDb inst_ = null;
}


