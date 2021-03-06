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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.DialogFragment;
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
import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.pushcoin.lib.integrator.IntentIntegrator;

public class BrowseCatalogActivity 
	extends SlidingActivity implements EditItemPropertiesFragment.OnDismissed
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );

		// Hide the title bar, leave status bar as is
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create the event pump
		EventHub.newInstance( this );

		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );

		// Bootstrap database before creating fragments
		AppDb.newInstance( this );

		// Session manager
		carts_ = CartManager.newInstance( this );

		// Set this activity UI layout
		setContentView(R.layout.browse_catalog_layout);
		// Layout for the behind-menu
		setBehindContentView(R.layout.tab_menu_fragment);

		// configure the SlidingMenu
		SlidingMenu menu = getSlidingMenu();
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.sliding_menu_shadow);
		menu.setBehindWidthRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);

		// Don't let device go to sleep.
		// TODO: Let user change this behavior in Settings
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		// Let PushCoin service know we are back
		Log.v( Conf.TAG, "bootstrapping integrator" );
		AppDb.getInstance().getIntegrator().bootstrap(this);
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "BrowseCatalogActivity" );
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// Remove self from the event hub.
		EventHub.getInstance().unregister( handler_ );
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
			.replace( R.id.hz_center_pane, browseCategory, FragmentTag.BROWSE_ITEMS_FRAG )
			.commit();
	}

	/** User clicks on an item from the browse-list. */
	private void onShoppingItemClicked(String itemId)
	{
		Log.v( Conf.TAG, "add-or-configure-item;itemId="+itemId );

		// Get the item
		Item item = AppDb.getInstance().getItemWithId( itemId, Conf.FIELD_PRICE_TAG_DEFAULT );

		// To add item to the cart, it must be defined
		if ( item.isDefined() ) {
			addItemToCart( item );
		}
		else // or, we need to configure the item first
		{
			getFragmentManager().beginTransaction()
				.replace( R.id.hz_center_pane, 
					ConfigureItemFragment.newInstance( item ), FragmentTag.CONFIGURE_ITEM_FRAG )
				.commit();
		}
	}

	/** User clicks Checkout -- kick of new activity */
	private void onCheckoutClicked()
	{
		Intent intent = new Intent(this, CheckoutActivity.class);
		startActivity( intent );
		// comment in if you like anim
		// overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	/** Adds item to cart, with option to set its properties. */
	private void addItemToCart( Item item )
	{
		// User can change item properties before adding it to cart
		if ( item.hasProperties() ) 
		{
			// DialogFragment.show() will add the fragment in a transaction, 
			// but we  need to remove any currently shown dialog.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag( Conf.DIALOG_EDIT_ITEM_PROPERTIES );
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the dialog.
			DialogFragment newFragment = EditItemPropertiesFragment.newInstance(item);
			newFragment.show(ft, Conf.DIALOG_EDIT_ITEM_PROPERTIES);
		}
		else {
			carts_.getActiveCart().add( Util.toCartCombo( item ) );
		}
	}

	@Override
	public void onEditItemPropertiesDone( Item item ) {
		carts_.getActiveCart().add( Util.toCartCombo( item ) );
	}

	private Handler handler_;
	private CartManager carts_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<BrowseCatalogActivity> ref_; 

		IncomingHandler(BrowseCatalogActivity ref) {
			ref_ = new WeakReference<BrowseCatalogActivity>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			BrowseCatalogActivity ref = ref_.get();
			if (ref != null)
			{
				switch( msg.what )
				{
					case MessageId.CATEGORY_CLICKED:
						ref.onShoppingCategoryClicked( (String) msg.obj );
					break;

					case MessageId.ITEM_CLICKED:
						ref.onShoppingItemClicked( (String) msg.obj );
					break;

					case MessageId.CHECKOUT_CLICKED:
						ref.onCheckoutClicked();
					break;
				}
			}
		}
	}
}

