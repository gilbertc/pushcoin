/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.app.bitsypos;

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
	static class Statement
	{
		Statement(String stmt, String[] args)
		{
			this.sql = stmt;
			this.args = args;
		}

		public final String sql;
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
	public ArrayList<Statement> generateDbStatements()
	{
		ArrayList<Statement> rs = new ArrayList<Statement>();
		try 
		{
			rs.addAll( genStmtCategory() );
			rs.addAll( genProduct() );
		}
		catch (JSONException e) {
			throw new RuntimeException( e.toString() );
		}
		return rs;
	}

	private ArrayList<Statement> genStmtCategory() throws JSONException
	{
		ArrayList<Statement> rs = new ArrayList<Statement>();
		JSONArray categories = json_.getJSONArray( Conf.FIELD_CATEGORY );

		for (int i = 0; i < categories.length(); ++i)
		{
			JSONObject entry = categories.getJSONObject(i);
			String[] args = new String[] { entry.getString( Conf.FIELD_NAME ), entry.getString( Conf.FIELD_TAG ) };
			rs.add( new Statement( Conf.STMT_CATEGORY_INSERT, args ) );
		}
		return rs;
	}

	private ArrayList<Statement> genItem(JSONObject product) throws JSONException
	{
		try 
		{
			ArrayList<Statement> rs = new ArrayList<Statement>();
			String[] args = new String[] 
				{
					product.getString( Conf.FIELD_ITEM_ID ),
					product.getString( Conf.FIELD_NAME ),
					product.optString( Conf.FIELD_IMAGE, "" ),
					product.optString( Conf.FIELD_TINT, "" ),
					Integer.toString( product.optInt( Conf.FIELD_ORDER, Short.MAX_VALUE ) )
				};
			rs.add( new Statement( Conf.STMT_ITEM_INSERT, args ) );
			return rs;
		} catch (JSONException e) {
			throw new RuntimeException( product.toString() + ": " + e.getMessage() );
		}
	}

	private ArrayList<Statement> genTag(JSONObject product) throws JSONException
	{
		try 
		{
			String item_id = product.getString( Conf.FIELD_ITEM_ID );

			ArrayList<Statement> rs = new ArrayList<Statement>();
			JSONArray tags = product.optJSONArray( Conf.FIELD_TAG );

			for (int i = 0; tags != null && i < tags.length(); ++i)
			{
				String[] args = new String[] { tags.getString( i ), item_id };
				rs.add( new Statement( Conf.STMT_TAGGED_ITEM_INSERT, args ) );
			}
			return rs;

		} catch (JSONException e) {
			throw new RuntimeException( product.toString() + ", tag error: " + e.getMessage() );
		}
	}

	private ArrayList<Statement> genRelated(JSONObject product) throws JSONException
	{
		try 
		{
			String item_id = product.getString( Conf.FIELD_ITEM_ID );

			ArrayList<Statement> rs = new ArrayList<Statement>();
			JSONArray related = product.optJSONArray( Conf.FIELD_RELATED_ITEM );

			for (int i = 0; related != null && i < related.length(); ++i)
			{
				String[] args = new String[] { item_id, related.getString( i ) };
				rs.add( new Statement( Conf.STMT_RELATED_ITEM_INSERT, args ) );
			}
			return rs;

		} catch (JSONException e) {
			throw new RuntimeException( product.toString() + ", related-item error: " + e.getMessage() );
		}
	}

	private ArrayList<Statement> genProperty(JSONObject product) throws JSONException
	{
		try 
		{
			String item_id = product.getString( Conf.FIELD_ITEM_ID );

			ArrayList<Statement> rs = new ArrayList<Statement>();
			JSONArray properties = product.optJSONArray( Conf.FIELD_ITEM_PROPERTY );

			for (int i = 0; properties != null && i < properties.length(); ++i)
			{
				String[] args = new String[] { item_id, properties.getString( i ), "N" };
				rs.add( new Statement( Conf.STMT_ITEM_PROPERTY_INSERT, args ) );
			}
			return rs;

		} catch (JSONException e) {
			throw new RuntimeException( product.toString() + ", item-modifier error: " + e.getMessage() );
		}
	}

	private ArrayList<Statement> genPrice(JSONObject product) throws JSONException
	{
		try 
		{
			String item_id = product.getString( Conf.FIELD_ITEM_ID );

			ArrayList<Statement> rs = new ArrayList<Statement>();

			JSONObject prices = product.optJSONObject( Conf.FIELD_PRICE );

			if (prices == null)
				return rs;

			JSONArray price_tags = prices.names();

			for (int i = 0; i < price_tags.length(); ++i)
			{
				String price_tag = price_tags.getString(i);
				String price = prices.get(price_tag).toString();

				String[] args = new String[] { item_id, price_tag, price };
				rs.add( new Statement( Conf.STMT_PRICE_INSERT, args ) );
			}
			return rs;

		} catch (JSONException e) {
			throw new RuntimeException( product.toString() + ", price error: " + e.getMessage() );
		}
	}

	private ArrayList<Statement> genCombo(JSONObject product) throws JSONException
	{
		try 
		{
			String item_id = product.getString( Conf.FIELD_ITEM_ID );

			ArrayList<Statement> rs = new ArrayList<Statement>();

			JSONArray combo = product.optJSONArray( Conf.FIELD_COMBO );

			if (combo == null)
				return rs;

			for (int i = 0; i < combo.length(); ++i)
			{
				JSONObject slot = combo.getJSONObject( i );

				String slot_name = slot.getString( Conf.FIELD_SLOT_NAME );
				String default_item_id = slot.optString( Conf.FIELD_SLOT_DEFAULT_ITEM, null );
				String choice_item_tag = slot.optString( Conf.FIELD_SLOT_ITEM_TAG, null );
				String quantity = Integer.toString( slot.optInt( Conf.FIELD_QUANTITY, 1 ) );
				String price_tag = slot.optString( Conf.FIELD_SLOT_PRICE_TAG, Conf.FIELD_PRICE_TAG_DEFAULT );
				
				// both cannot be null
				if (default_item_id == null && choice_item_tag == null) {
					throw new RuntimeException( product.toString() + ", combo error: no choice_tag and no default item provided for slot: " + slot_name);
				}

				String[] args = new String[] { item_id, slot_name, default_item_id, choice_item_tag, quantity, price_tag };
				rs.add( new Statement( Conf.STMT_COMBO_ITEM_INSERT, args ) );
			}
			return rs;

		} catch (JSONException e) {
			throw new RuntimeException( product.toString() + ", combo error: " + e.getMessage() );
		}
	}

	private ArrayList<Statement> genProduct() throws JSONException
	{
		ArrayList<Statement> rs = new ArrayList<Statement>();
		JSONArray products = json_.getJSONArray( Conf.FIELD_PRODUCT );

		for (int i = 0; i < products.length(); ++i)
		{
			JSONObject product = products.getJSONObject(i);
			// every product must have an underlying item entry
			rs.addAll( genItem( product ) ); 
			// product tags
			rs.addAll( genTag( product ) ); 
			// product prices
			rs.addAll( genPrice( product ) ); 
			// related products
			rs.addAll( genRelated( product ) ); 
			// item properties
			rs.addAll( genProperty( product ) ); 
			// combo items need extra processing
			rs.addAll( genCombo( product ) ); 
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

	// Everything below is used during testing only
	//
	private static int MAX_INPUT_LENGTH = 50*1024; // 50kb

	public static String copyStreamToString(InputStream is) 
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
				input = System.in;
			}
			else {
				System.out.println( "Reading from file: " + args[0] );
				input = new FileInputStream( args[0] );
			}
			String jsonData = copyStreamToString( input );
			JsonImporter imp = new JsonImporter( jsonData );

			// show what's produced
			for ( Statement stmt: imp.generateDbStatements() ) 
			{
				String sql = stmt.sql.replace("?", "'%s'");
				String prettyStmt = String.format(sql, (Object[])stmt.args) + ";";
				System.out.println( prettyStmt.replace("'null'", "null") );
			}
		}
		catch (Exception e) {
			// System.err.println(e.getMessage());
			e.printStackTrace();
		}

		System.exit(1);
	}
}

