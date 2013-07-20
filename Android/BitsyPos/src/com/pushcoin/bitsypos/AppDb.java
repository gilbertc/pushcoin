package com.pushcoin.bitsypos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class AppDb extends SQLiteAssetHelper {

	private static final String DATABASE_NAME = "bitsypos";
	private static final int DATABASE_VERSION = 1;

	public Cursor getHelloWorld() 
	{
		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String [] sqlSelect = {"one", "two"};
		String sqlTables = "tbl1";

		qb.setTables(sqlTables);
		Cursor c = qb.query(db, sqlSelect, null, null, null, null, null);

		c.moveToFirst();
		return c;
	}

	public static AppDb getInstance(Context ctx) 
	{
		/** 
		 * use the application context as suggested by CommonsWare.
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
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private AppDb(Context ctx) 
		{
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
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


