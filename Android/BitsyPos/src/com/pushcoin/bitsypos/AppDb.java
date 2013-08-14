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

public class AppDb extends SQLiteAssetHelper 
{
	/**
		Get labels.
	*/
	public ArrayList<Category> getMainCategories() 
	{ 
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery( Conf.SQL_GET_MAIN_CATEGORIES, null );

		ArrayList<Category> rs = new ArrayList<Category>();

		if ( !c.moveToFirst() ) {
			return rs;
		}

		do 
		{
			Category cat = new Category();
			cat.category_id = c.getString(0);
			cat.tag_id = c.getString(1);

			rs.add( cat );
			Log.v(Conf.TAG, "main-category|name="+cat.category_id+";tag="+cat.tag_id);
		}
		while (c.moveToNext());

		return rs;
	}

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

	private void initSample()
	{
		// Sample data found, begin import
		SQLiteDatabase db = getReadableDatabase();

		try
		{
			// load sample data
			AssetManager assetManager = ctx_.getAssets();
			InputStream input = assetManager.open(Conf.SAMPLE_DATA_FILE);
			Log.i(Conf.TAG, "loading-sample-data|file="+Conf.SAMPLE_DATA_FILE);

			String jsonData = JsonImporter.copyStreamToString( input );
			JsonImporter imp = new JsonImporter( jsonData );

			// wipe old data first
			for (String table: Conf.PRODUCT_TABLES) {
				db.delete( table, null, null );
			}

			db.beginTransaction();
			// insert new records
			for ( JsonImporter.Statement stmt: imp.generateDbStatements() )
			{
				// Log.v(Conf.TAG, "sample|stmt="+stmt.sql+";args="+java.util.Arrays.toString(stmt.args));
				SQLiteStatement complStmt = getStatement( db, stmt.sql );
				int ix = 1;
				for (String arg: stmt.args)
				{
					if (arg != null) {
						complStmt.bindString( ix, arg );
					}
					++ix;
				}
				complStmt.execute();
			}
			db.setTransactionSuccessful();
		}
		catch (IOException e) {
			Log.w(Conf.TAG, "no-sample-data|file="+Conf.SAMPLE_DATA_FILE);
		}
		finally {
			db.endTransaction();
		}
	}

	private SQLiteStatement getStatement(SQLiteDatabase db, String stmt)
	{
		SQLiteStatement ret = stmtCache_.get( stmt );
		if ( ret == null )
		{
			ret = db.compileStatement( stmt );
			stmtCache_.put( stmt, ret );
		}
		return ret;
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
	
		// Activity context
		ctx_ = ctx;

		// Compiled-statements cache
		stmtCache_ = new TreeMap<String, SQLiteStatement>();

		// Populate sample data
		initSample();
	}

	private Context ctx_;
	private static AppDb inst_ = null;
	TreeMap<String, SQLiteStatement> stmtCache_;
}