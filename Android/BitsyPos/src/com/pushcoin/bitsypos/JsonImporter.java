package com.pushcoin.bitsypos;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Formatter;

public class JsonImporter
{
	static private class Statement
	{
		Statement(String stmt, String[] args)
		{
			this.stmt = stmt;
			this.args = args;
		}

		public final String stmt;
		public final String[] args;
	}

	public JsonImporter( String jsonData )
	{
		// create json object
		json_ = fromString( jsonData );
	}

	/**
		Uses JSON input to generate Bitsy DB statements.
	*/
	private ArrayList<Statement> generateDbStatements()
	{
		ArrayList<Statement> rs = new ArrayList<Statement>();
		try 
		{
			rs.addAll( genStmtCategories() );
		}
		catch (JSONException e) {
			throw new RuntimeException( e.toString() );
		}
		return rs;
	}

	private ArrayList<Statement> genStmtCategories() throws JSONException
	{
		ArrayList<Statement> rs = new ArrayList<Statement>();
		JSONArray categories = json_.getJSONArray( FIELD_CATEGORY );

		for (int i = 0; i < categories.length(); ++i)
		{
			JSONObject entry = categories.getJSONObject(i);
			String[] args = new String[] { entry.getString( FIELD_NAME ), entry.getString( FIELD_TAG ) };
			rs.add( new Statement( STMT_CATEGORY_INSERT, args ) );
		}
		return rs;
	}

	private static JSONObject fromString( String jsonData )
	{
		// try parse the string to a JSON object
		try {
			return new JSONObject( jsonData );
		} catch (JSONException e) {
			throw new RuntimeException( e.toString() );
		}
	}

	JSONObject json_;

	private final String FIELD_CATEGORY = "category";
	private final String FIELD_NAME = "name";
	private final String FIELD_TAG = "tag";

	private final String STMT_CATEGORY_INSERT = "insert into catgory (category_id, tag_id) values( %, % )";

	// Everything below is used during testing only
	//
	private static int MAX_INPUT_LENGTH = 50*1024; // 50kb

	public static String convertStreamToString(InputStream is) 
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static void main(String[] args)
	{
		try
		{
			InputStream input;

			if (args.length < 1) 
			{
				System.out.println( "Reading from STDIN..." );
				input = System.in;
			}
			else {
				System.out.println( "Reading from file: " + args[0] );
				input = new FileInputStream( args[0] );
			}
			String jsonData = convertStreamToString( input );
			JsonImporter imp = new JsonImporter( jsonData );

			// show what's produced
			for ( Statement stmt: imp.generateDbStatements() ) {
				System.out.println( stmt.stmt + ": " + java.util.Arrays.toString(stmt.args) );
			}

			System.out.println( "Done" );
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(1);
	}
}

