package com.pushcoin.bitsypos;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.util.Log;
import java.util.ArrayList;

public class BitsyPosActivity 
	extends Activity implements IDispatcher
{
	private static final String TAG = "BitsyPos";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	}

	/** Used by fragments to access our dispatcher. */
	@Override
	public Handler getDispachable() 
	{
		return handler_;
	}
	
	/** Dispatch events coming from fragments. */
	private Handler handler_ = new Handler() {
		@Override
		public void handleMessage(Message msg) 
		{
			Log.v(TAG, "event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
			switch( msg.what )
			{
				case MessageId.SHOPPING_CATEGORY_CLICKED:
				onShoppingCategoryClicked(msg.arg1);
				break;
			}
		}
	};

	public static void printViewHierarchy(ViewGroup $vg, String $prefix)
	{
			for (int i = 0; i < $vg.getChildCount(); i++) {
					View v = $vg.getChildAt(i);
					String desc = String.format("%s|[%d/%d] %s ID:0x%x", $prefix, i, $vg.getChildCount()-1, v.getClass().getSimpleName(), v.getId());
					Log.v(TAG, desc);

					if (v instanceof ViewGroup) {
							printViewHierarchy((ViewGroup)v, desc);
					}
			}
	}

	/** User clicks on category item. */
	private void onShoppingCategoryClicked(int pos)
	{
		FragmentManager fragmentManager = getFragmentManager();
	
		ViewGroup panel_w1 = (ViewGroup)findViewById(R.id.panel_w1);
		// printViewHierarchy(panel_w1, "panel_w1");

		// Fragment _is_ the grid view
		GridView view = (GridView)fragmentManager.findFragmentById(R.id.shopping_item_list_frag).getView();

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
	}

	final int icons_[] = {
		R.drawable.apple,
		R.drawable.babelfish,
		R.drawable.cake,
		R.drawable.cake2,
		R.drawable.cheese_cake,
		R.drawable.coffee_cup,
		R.drawable.corbeille_box,
		R.drawable.cup_cake,
		R.drawable.fries,
		R.drawable.fruit_cake,
		R.drawable.ic_launcher,
		R.drawable.pizza,
		R.drawable.soft_drink,
		R.drawable.sushi_maki,
		R.drawable.toast,
		R.drawable.wine_glass, 
	};
}
