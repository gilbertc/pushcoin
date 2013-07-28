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
import android.widget.ListView;
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
		View scrollLayout = inflater.inflate(R.layout.configure_item_view, container, false);
		LinearLayout layout = (LinearLayout) scrollLayout.findViewById( R.id.item_configuration_arena );

		// Populate related list
		ArrayList<Item> relatedItems = item_.getRelatedItems();
		if ( !relatedItems.isEmpty() ) 
		{
			ListView view = (ListView)inflater.inflate(R.layout.related_items_view, layout, false);

			ArrayList<IconLabelArrayAdapter.Entry> menuItems = 
				new ArrayList<IconLabelArrayAdapter.Entry>();

			for ( Item item : relatedItems )
			{
				Log.v(Conf.TAG, "related-item|name="+item.getName() );
				menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.coffee_cup, item.getName()) );
			}

			view.setAdapter(
				new IconLabelArrayAdapter(
					ctx, 
					R.layout.shopping_category_menu_row, 
					R.id.shopping_category_menu_icon, 
					R.id.shopping_category_menu_label, 
					menuItems) );

			// add the listview to the layout
			layout.addView( view );
		}

		// Similarly, we create a list-view for each slot being configured
		for ( Slot slot : item_.getSlots() )
		{
			ListView view = (ListView)inflater.inflate(R.layout.slot_items_view, layout, false);

			ArrayList<IconLabelArrayAdapter.Entry> menuItems = 
				new ArrayList<IconLabelArrayAdapter.Entry>();

			for ( Item item : slot.getAlternatives() )
			{
				Log.v(Conf.TAG, "alternative-item|name="+item.getName() );
				menuItems.add( new IconLabelArrayAdapter.Entry(R.drawable.coffee_cup, item.getName()) );
			}

			view.setAdapter(
				new IconLabelArrayAdapter(
					ctx, 
					R.layout.shopping_category_menu_row, 
					R.id.shopping_category_menu_icon, 
					R.id.shopping_category_menu_label, 
					menuItems) );

			// add the listview to the layout
			layout.addView( view );
		}

		// install click-event listener
		// view.setOnItemClickListener(new ListSelection());

		return scrollLayout;
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
