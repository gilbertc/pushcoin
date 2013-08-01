package com.pushcoin.bitsypos;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.ArrayList;

public class ConfigureItemFragment extends Fragment 
{
	public ConfigureItemFragment( String backstackId, Item item )
	{
		assert item != null: "cannot configure a non-existing item";
		item_ = item;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context ctx = getActivity();

		// Store activity's dispacher
		dispatchable_ = ((IDispatcher)ctx).getDispachable();

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.configure_item_view, container, false);

		// Set combo name
		TextView comboName = (TextView) fragmentRootLayout.findViewById( R.id.slot_items_combo_name );
		comboName.setText( item_.getName() );

		// Create a list-view for each slot being configured
		LinearLayout layoutSlots = (LinearLayout) fragmentRootLayout.findViewById( R.id.item_configuration_arena );
		for ( Slot slot : item_.getSlots() )
		{
			View slotLayout = inflater.inflate(R.layout.slot_items_view, layoutSlots, false);

			// set header to slot name
			TextView title = (TextView) slotLayout.findViewById( R.id.slot_items_header );
			title.setText( slot.getName() );

			// populate list view with items
			ListView listview = (ListView) slotLayout.findViewById( R.id.slot_items_listview );

			ArrayList<IconLabelArrayAdapter.Entry> menuItems = 
				new ArrayList<IconLabelArrayAdapter.Entry>();

			for ( Item item : slot.getAlternatives() )
			{
				Log.v(Conf.TAG, "alternative-item|name="+item.getName() );
				menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.coffee_cup, item.getName()) );
			}

			listview.setAdapter(
				new IconLabelArrayAdapter(
					ctx, 
					R.layout.configure_item_slot_row, 
					R.id.shopping_category_menu_icon, 
					R.id.shopping_category_menu_label, 
					menuItems) );

			// add this slot to the layout
			layoutSlots.addView( slotLayout );
		}

		// Populate related list
		ArrayList<Item> relatedItems = item_.getRelatedItems();
		if ( !relatedItems.isEmpty() ) 
		{
			GridView relatedItemsView = (GridView) fragmentRootLayout.findViewById( R.id.configure_related_items );

			ArrayList<IconLabelArrayAdapter.Entry> menuItems = 
				new ArrayList<IconLabelArrayAdapter.Entry>();

			for ( Item item : relatedItems )
			{
				Log.v(Conf.TAG, "related-item|name="+item.getName() );
				menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.coffee_cup, item.getName()) );
			}

			relatedItemsView.setAdapter(
				new IconLabelArrayAdapter(
					ctx, 
					R.layout.shopping_category_menu_row, 
					R.id.shopping_category_menu_icon, 
					R.id.shopping_category_menu_label, 
					menuItems) );
		}

		TextView addToCart = (TextView) fragmentRootLayout.findViewById(R.id.slot_items_add_to_cart);
		addToCart.setTypeface( Typeface.createFromAsset(ctx.getAssets(), "fonts/modernpics.otf") );

		// install click-event listener
		// view.setOnItemClickListener(new ListSelection());

		return fragmentRootLayout;
	}

	/*
	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Message m = dispatchable_.obtainMessage(MessageId.SHOPPING_ITEM_CLICKED, position, 0);
			m.sendToTarget();
		}
	}
	*/

	private Handler dispatchable_ = null;
	private Item item_;
}
