package com.pushcoin.app.bitsypos;

import android.util.Log;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.BitmapFactory;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.math.BigDecimal;
import java.io.InputStream;
import java.io.IOException;

public class AppDb extends SQLiteAssetHelper 
{
	/**
		Find a customer with keywords.
	*/
	interface FindCustomerWithKeywordReply
	{
		void onCustomerRecordFound(List<Customer> data);
	}

	public void asyncFindCustomerWithKeyword( String keyword, FindCustomerWithKeywordReply handler )
	{
		List<Customer> res = new ArrayList();
		// populate sample
		Customer c1 = Customer.newInstance();
		c1.accountId = "CAX5PNCPKC";
		c1.firstName = "Slawomir";
		c1.lastName = "Lisznianski";
		c1.title = "Creative Guru";
		c1.identifier = "123-123-3123";
		c1.mugshot = BitmapFactory.decodeResource( appCtx_.getResources(), R.drawable.contrib_sl );
		c1.balance = new BigDecimal("56.32");
		res.add( c1 );

		Customer c2 = Customer.newInstance();
		c2.accountId = "CAX5RNCPKC";
		c2.firstName = "Eng";
		c2.lastName = "Choong";
		c2.title = "Coding Ninja";
		c2.identifier = "221-823-3123";
		c2.mugshot = BitmapFactory.decodeResource( appCtx_.getResources(), R.drawable.contrib_ec );
		c2.balance = new BigDecimal("156.32");
		res.add( c2 );

		Customer c3 = Customer.newInstance();
		c3.accountId = "KAX5RNCPKC";
		c3.firstName = "Gilbert";
		c3.lastName = "Cheung";
		c3.title = "Hardware Hacker";
		c3.identifier = "331-823-3123";
		c3.mugshot = BitmapFactory.decodeResource( appCtx_.getResources(), R.drawable.contrib_gc );
		c3.balance = new BigDecimal("88.10");
		res.add( c3 );

		Customer c4 = Customer.newInstance();
		c4.accountId = "LAX5RNCPKC";
		c4.firstName = "Lucas";
		c4.lastName = "Lisznianski";
		c4.title = "8th Grade Student";
		c4.identifier = "331-823-3123";
		c4.mugshot = BitmapFactory.decodeResource( appCtx_.getResources(), R.drawable.contrib_lucas );
		c4.balance = new BigDecimal("8.10");
		res.add( c4 );

		Customer c5 = Customer.newInstance();
		c5.accountId = "JJX5RNCPKC";
		c5.firstName = "Milosh";
		c5.lastName = "Lisznianski";
		c5.title = "4th Grade Student";
		c5.identifier = "881-823-3123";
		c5.mugshot = BitmapFactory.decodeResource( appCtx_.getResources(), R.drawable.contrib_milosh );
		c5.balance = new BigDecimal("1.00");
		res.add( c5 );

		handler.onCustomerRecordFound( res );
	}

	/**
		Get labels.
	*/
	public ArrayList<Category> getMainCategories() 
	{ 
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery( Conf.SQL_GET_MAIN_CATEGORIES, null );

		try 
		{
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
		} finally {
			c.close();
		}
	}

	/**
		Finds an item by ID
	*/
	public Item getItemWithId( String itemId, String priceTag ) 
	{ 
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery( Conf.SQL_FETCH_ITEM_BY_ID, new String[]{ priceTag, itemId } );

		try 
		{
			if ( !c.moveToFirst() ) {
				throw new BitsyError("No such item found: " + itemId);
			}
			return createItemFromCursor(c, Conf.ITEM_IN_CURSOR_T0);
		} 
		finally {
			c.close();
		}
	}

	/**
		Get items matching a given tag.
	*/
	public List<Item> findItemsWithTag( String itemTag, String priceTag ) 
	{ 
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery( Conf.SQL_FETCH_ITEMS_BY_TAG, new String[]{ priceTag, itemTag } );

		return createItemsFromCursor( c, new ItemFromCursorAdapter()
			{
				@Override
				public Item make( Cursor c ) {
					return createItemFromCursor(c, Conf.ITEM_IN_CURSOR_T0);
				}
			});
	}

	/**
		Factory-interface for creating items from a cursor.
	*/
	interface ItemFromCursorAdapter {
		Item make( Cursor c );	
	}

	List<Item> createItemsFromCursor( Cursor c, ItemFromCursorAdapter factory )
	{
		List<Item> items = new ArrayList<Item>();

		try 
		{
			if ( c.moveToFirst() )
			{
				do {
					items.add( factory.make( c ) );
				} 
				while ( c.moveToNext() );
			}
		}
		finally {
			c.close();
		}
		return items;
	}

	/**
		Constucts an approprate item instance (basic or combo)
		from the provided DB cursor: item-id, name, price, children-count.

		Cursor position is not changed when this function returns.
	*/
	public Item createItemFromCursor( Cursor c, Map<String, Integer> field)
	{
		String id = c.getString( field.get(Conf.FIELD_ITEM_ID) );
		String name = c.getString( field.get(Conf.FIELD_NAME) );
		BigDecimal price = new BigDecimal( c.getString( field.get(Conf.FIELD_PRICE) ) );
		Map<String, String> properties = Util.splitProperties( c.getString( field.get(Conf.FIELD_ITEM_PROPERTY) ) );
		int children = c.getInt( field.get(Conf.FIELD_ITEM_CHILDREN) );

		// return basic combo item
		return (children == 0) ?
			new BasicItem( id, name, price, properties ) : 
			new ComboItem( id, name, price, properties, children );
	}

	/**
		Constucts a slot from the provided DB cursor: 
		parent, slot-name, default_item_id, choice_item_tag, price_tag

		Cursor position is not changed when this function returns.
	*/
	public Item createSlotFromCursor( Cursor c )
	{
		String parentItemId = c.getString(0);
		String slotName = c.getString(1);
		String slotPriceTag = c.getString(2);
		String choiceItemTag = c.getString(3);
		String defaultItemId = c.getString(4);

		// If default item present, create it too
		Item defaultItem = null;
		if ( !defaultItemId.isEmpty() ) {
			defaultItem = createItemFromCursor(c, Conf.ITEM_IN_CURSOR_T1);
		}

		// if there are no choices, we return default item
		if ( choiceItemTag.isEmpty() ) {
			return defaultItem;
		} else {
			return new SlotItem( parentItemId, slotName, slotPriceTag, choiceItemTag, defaultItem );
		}
	}

	public static AppDb newInstance(Context ctx) 
	{
		if (inst_ == null) {
			inst_ = new AppDb(ctx.getApplicationContext());
		}
		return inst_;
	}

	public static AppDb getInstance() 
	{
		if (inst_ == null) {
			throw new BitsyError("Did you forget to call AppDb.newInstance in Activity?");
		}
		return inst_;
	}

	private void initSample(Context ctx)
	{
		// Sample data found, begin import
		SQLiteDatabase db = getReadableDatabase();

		try
		{
			// load sample data
			AssetManager assetManager = ctx.getAssets();
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
				//Log.v(Conf.TAG, "db|stmt="+stmt.sql+";args="+java.util.Arrays.toString(stmt.args));
				SQLiteStatement complStmt = getStatement( db, stmt.sql );
				int ix = 1;
				for (String arg: stmt.args)
				{
					if (arg == null) {
						complStmt.bindNull( ix );
					}
					else {
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
		Private constructor ensures that direct instatiateion isn't possible.

		Code directly in Activity calls newInstance(..)
		All non-activity code uses getInstance(), at which point this singleton
		has to be already constructed.
	*/
	private AppDb(Context ctx) 
	{
		super(ctx, Conf.DATABASE_NAME, null, Conf.DATABASE_VERSION);
		// you can use an alternate constructor to specify a database location
		// (such as a folder on the sd card)
		// you must ensure that this folder is available and you have permission
		// to write to it
		//super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
	
		// Store app context
		appCtx_ = ctx;

		// Compiled-statements cache
		stmtCache_ = new TreeMap<String, SQLiteStatement>();

		// Populate sample data
		initSample(ctx);
	}

	private static AppDb inst_;
	Context appCtx_;
	private TreeMap<String, SQLiteStatement> stmtCache_;
}
