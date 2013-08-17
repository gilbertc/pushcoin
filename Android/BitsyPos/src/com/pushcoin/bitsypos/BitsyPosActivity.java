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
		setContentView(R.layout.shopping_main);

		// Store message dispatcher for the cart fragment
		FragmentManager fragmentManager = getFragmentManager();
		cartFragmentHandler_ = ((IDispatcher)fragmentManager.findFragmentById(R.id.shopping_cart_frag)).getDispachable();

		AppDb db = AppDb.getInstance(this);
		ArrayList<Item> items = db.findItems( "breakfast", Conf.FIELD_PRICE_TAG_DEFAULT );
		if ( items.isEmpty() ) {
			Log.v(Conf.TAG, "no-items-found|tag=breakfast");
		}
		else 
		{
			ConfigureItemFragment itemList = new ConfigureItemFragment("uuid", items.get(0));
			itemList.setArguments(getIntent().getExtras());

			getFragmentManager().beginTransaction()
				.add( R.id.hz_center_pane, itemList, FragmentTag.CONFIGURE_ITEM )
				.commit();
		}
		
//	ShoppingItemListFragment itemList = new ShoppingItemListFragment();
//	itemList.setArguments(getIntent().getExtras());
//	
//	getFragmentManager().beginTransaction()
//		.add( R.id.hz_center_pane, itemList, FragmentTag.SHOPPING_ITEM_LIST )
//		.commit();

		// Don't let device go to sleep.
		// TODO: Let user change this behavior in Settings
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
					onShoppingCategoryClicked(msg.arg1);
				break;

				case MessageId.SHOPPING_ITEM_CLICKED:
					onShoppingItemClicked(msg.arg1);
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
			for (int i = 0; i < $vg.getChildCount(); i++) {
					View v = $vg.getChildAt(i);
					String desc = String.format("%s|[%d/%d] %s ID:0x%x", $prefix, i, $vg.getChildCount()-1, v.getClass().getSimpleName(), v.getId());
					Log.v(Conf.TAG, desc);

					if (v instanceof ViewGroup) {
							printViewHierarchy((ViewGroup)v, desc);
					}
			}
	}

	/** User clicks on category item. */
	private void onShoppingCategoryClicked(int pos)
	{
		/*
		FragmentManager fragmentManager = getFragmentManager();
	
		ViewGroup panel_w1 = (ViewGroup)findViewById(R.id.panel_w1);
		// printViewHierarchy(panel_w1, "panel_w1");

		GridView view = (GridView)fragmentManager.findFragmentByTag(FragmentTag.SHOPPING_ITEM_LIST).getView();

		ArrayList<ItemSummaryArrayAdapter.Entry> items = 
			new ArrayList<ItemSummaryArrayAdapter.Entry>();

		// populate item(s)
		for (int i = 0; i < pos+10; ++i)
		{
			items.add( new ItemSummaryArrayAdapter.Entry(icons_[i%icons_.length], "Coffee", "Maine Roasted", "$3.75", R.drawable.cart_empty_gray) );
		}

		ItemSummaryArrayAdapter adapter =		
			new ItemSummaryArrayAdapter(this, R.layout.shopping_item_list_block, R.id.shopping_item_list_product, R.id.shopping_item_list_title, R.id.shopping_item_list_desc, R.id.shopping_item_list_price, R.id.shopping_item_list_indicator, items);

		view.setAdapter( adapter );
		*/
	}

	/** User clicks on item. */
	private void onShoppingItemClicked(int pos)
	{
		ShoppingPanelFragment newFragment = new ShoppingPanelFragment();
		Bundle args = new Bundle();
		newFragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		// Remove categories
		Fragment categoryMenu = fragmentManager.findFragmentByTag(FragmentTag.SHOPPING_CATEGORY_MENU);
		transaction.remove(categoryMenu);

		// Replace items with the combo editor
		transaction.replace(R.id.hz_center_pane, newFragment);

		// and add the transaction to the back stack so the user can navigate back
		transaction.addToBackStack(null);
		transaction.commit();
	}

	Handler cartFragmentHandler_;
}
