package com.pushcoin.bitsypos;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;

public class BitsyPosActivity 
	extends Activity implements IDispatcher
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Bootstrap database before creating fragments
		AppDb.newInstance( this );

		setContentView(R.layout.shopping_main);

		// Session manager
		access_ = SessionManager.getInstance( this );

		// Store message dispatcher for the cart fragment
		FragmentManager fragmentManager = getFragmentManager();
		cartFragmentHandler_ = ((IDispatcher)fragmentManager.findFragmentById(R.id.shopping_cart_frag)).getDispachable();

		// Don't let device go to sleep.
		// TODO: Let user change this behavior in Settings
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/** Used by fragments to access our dispatcher. */
	@Override
	public Handler getDispachable() 
	{
		return handler_;
	}
	
	/** Dispatch events. */
	private Handler handler_ = new Handler() {
		@Override
		public void handleMessage(Message msg) 
		{
			Log.v(Conf.TAG, "place=activity;event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
			switch( msg.what )
			{
				case MessageId.SHOPPING_CATEGORY_CLICKED:
					onShoppingCategoryClicked( (String) msg.obj );
				break;

				case MessageId.SHOPPING_ITEM_CLICKED:
					onShoppingItemClicked( (String) msg.obj );
				break;

				case MessageId.CART_CONTENT_CHANGED:
					// Forward cart-modified message.
					cartFragmentHandler_.sendMessage( Message.obtain(msg) );
				break;
			}
		}
	};

	public static void printViewHierarchy(ViewGroup $vg, String $prefix)
	{
		for (int i = 0; i < $vg.getChildCount(); i++)
		{
			View v = $vg.getChildAt(i);
			String desc = String.format("%s|[%d/%d] %s ID:0x%x", $prefix, i, $vg.getChildCount()-1, v.getClass().getSimpleName(), v.getId());
			Log.v(Conf.TAG, desc);

			if (v instanceof ViewGroup) {
				printViewHierarchy((ViewGroup)v, desc);
			}
		}
	}

	/** User clicks on category item. */
	private void onShoppingCategoryClicked(String categoryTag)
	{
		Log.v( Conf.TAG, "browse-category;name="+categoryTag );

		FragmentManager fragmentManager = getFragmentManager();

		// Crate category-browser passing it category tag
		BrowseItemsFragment browseCategory = new BrowseItemsFragment();
		// Only one arg: category-tag
		Bundle args = new Bundle(1);
		args.putString(	Conf.FIELD_CATEGORY, categoryTag );
		browseCategory.setArguments( args );

		// Replace the fragment without appending to back stack
		getFragmentManager().beginTransaction()
			.replace( R.id.hz_center_pane, browseCategory, FragmentTag.SHOPPING_ITEM_LIST )
			.commit();
	}

	/** User clicks on item. */
	private void onShoppingItemClicked(String itemId)
	{
		Log.v( Conf.TAG, "add-or-configure-item;itemId="+itemId );

		// Get the item
		Item item = AppDb.getInstance().getItemWithId( itemId, Conf.FIELD_PRICE_TAG_DEFAULT );

		// If our item is defined, we can add it to the cart
		if ( item.isDefined() )
		{
			Cart cart = (Cart) access_.session( Conf.SESSION_CART );
			cart.add( Util.toCartCombo( item ) );
		}
		else // or, we need to configure the item first
		{
			ConfigureItemFragment configureItem = new ConfigureItemFragment("uuid", item);

			getFragmentManager().beginTransaction()
				.replace( R.id.hz_center_pane, configureItem, FragmentTag.CONFIGURE_ITEM )
				.commit();
		}
	}

	private SessionManager access_;
	private Handler cartFragmentHandler_;
}
