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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
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

		// Session manager
		access_ = SessionManager.getInstance( ctx );

		// Store activity's dispacher
		dispatchable_ = ((IDispatcher)ctx).getDispachable();

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.configure_item_view, container, false);

		// Set combo name
		Button comboName = (Button) fragmentRootLayout.findViewById( R.id.add_combo_item_to_cart );
		comboName.setText( "Add " + item_.getName() );

		// Create a list-view for each slot being configured
		LinearLayout layoutSlots = (LinearLayout) fragmentRootLayout.findViewById( R.id.item_configuration_arena );
		for ( final Slot slot : item_.getSlots() )
		{
			View slotLayout = inflater.inflate(R.layout.slot_items_view, layoutSlots, false);

			// set header to slot name
			TextView title = (TextView) slotLayout.findViewById( R.id.slot_items_header );
			title.setText( slot.getName() );

			// populate list view with items
			ListView listview = (ListView) slotLayout.findViewById( R.id.slot_items_listview );
			listview.setAdapter(
				new ArrayAdapter<Item>(
					ctx, 
					R.layout.configure_item_slot_row, 
					R.id.configure_slot_listview_item_label, 
					slot.getAlternatives()) );

			// on slot-item clicked
			listview.setOnItemClickListener(new OnSlotItemClicked(slot));

			// add this slot to the layout
			layoutSlots.addView( slotLayout );
		}

		// Populate related items

		relatedItems_ = item_.getRelatedItems(Conf.FIELD_PRICE_TAG_DEFAULT);
		if ( !relatedItems_.isEmpty() ) 
		{
			AutofitGridView relatedItemsView = (AutofitGridView) fragmentRootLayout.findViewById( R.id.configure_related_items );

			relatedItemsView.setAdapter(
				new RelatedItemListAdapter(
					ctx, 
					R.layout.related_item_menu_row, 
					R.id.related_item_label, 
					R.id.related_item_price, 
					relatedItems_) );

			// on related-item clicked
			relatedItemsView.setOnItemClickListener(new OnRelatedItemClicked());
			// Fit as many columns as possible
			relatedItemsView.setColumnWidth( relatedItemsView.measureMaxChildWidth() );
		}

		return fragmentRootLayout;
	}

	private class OnSlotItemClicked implements OnItemClickListener
	{
		public OnSlotItemClicked(Slot slot) {
			slot_ = slot;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Item item = slot_.getItem(position);
			Log.v(Conf.TAG, "slot-item-clicked|slot="+slot_.getName()+";name="+item.getName() );
		}
		private final Slot slot_;
	}

	private class OnRelatedItemClicked implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Item item = relatedItems_.get(position);
			Log.v(Conf.TAG, "related-item-clicked|name="+item.getName() );

			// TODO: Add to cart if item isDefined, otherwise start another
			// configure-item screen
			Cart cart = (Cart) access_.session( Conf.SESSION_CART );
			cart.add( item );
			//Handler receiver = cart.getDispachable();
			//Message msg = receiver.obtainMessage(MessageId.CART_ADD_ITEM, item);
			//receiver.sendMessageDelayed( msg, 500 );
		}
	}

	private SessionManager access_;
	private Handler dispatchable_;
	private Item item_;
	ArrayList<Item> relatedItems_;
}
