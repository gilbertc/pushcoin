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
import java.util.List;

public class ConfigureItemFragment extends Fragment 
{
	public ConfigureItemFragment( String backstackId, Item parent ) {
		parent_ = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		final Context ctx = getActivity();

		// Session manager
		access_ = SessionManager.getInstance( ctx );

		// Store activity's dispacher
		dispatchable_ = ((IDispatcher)ctx).getDispachable();

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.configure_item_view, container, false);

		// Set combo name
		final Button addToCartBtn = (Button) fragmentRootLayout.findViewById( R.id.add_combo_item_to_cart );
		addToCartBtn.setText( "Add " + parent_.getName() );
		// on click, add item to cart
		addToCartBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {
				Cart cart = (Cart) access_.session( Conf.SESSION_CART );
				cart.add( Util.toCartCombo(parent_) );
			}
		});

		// Create a list-view for each slot being configured
		LinearLayout layoutSlots = (LinearLayout) fragmentRootLayout.findViewById( R.id.item_configuration_arena );
		for ( final Item slot : parent_.getChildren() )
		{
			View slotLayout = inflater.inflate(R.layout.slot_items_view, layoutSlots, false);
			final TextView title = (TextView) slotLayout.findViewById( R.id.slot_items_header );
			ListView listview = (ListView) slotLayout.findViewById( R.id.slot_items_listview );

			// Slot header borrows name from its default item.
			title.setText( slot.getName() );

			if ( slot.isDefined() ) {
				title.setTextColor(ctx.getResources().getColor(R.color.DarkBlue));
			}

			// Fetch products which qualify as slot alternatives
			listview.setAdapter(
				new ArrayAdapter<Item>(
					ctx, 
					R.layout.configure_item_slot_row, 
					R.id.configure_slot_listview_item_label, 
					slot.getAlternatives()) );

			listview.setLongClickable(false);

			// on slot-item clicked
			listview.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						Item slotItem = slot.getAlternatives().get( position );
						Log.v(Conf.TAG, "slot-item-clicked|slot="+slot.getName()+";name="+slotItem.getName() );
						// TODO: what if still not defined? this will throw...
						// create another screen to configure, then go recursively until all configured
						parent_ = parent_.replace( position, slotItem );
						title.setText( slotItem.getName() );
						title.setTextColor(ctx.getResources().getColor(R.color.DarkBlue));

						// Re-evalute if all slots are configured and user can add item to cart.
						enableAddToCart( parent_.isDefined() );
					}
				});

			// add this slot to the layout
			layoutSlots.addView( slotLayout );
		}

		// If all slots have defaults, user can add this item to the cart right away
		enableAddToCart( parent_.isDefined() );

		// Populate related items

		relatedItems_ = parent_.getRelatedItems();
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
			relatedItemsView.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Item item = relatedItems_.get(position);
					Log.v(Conf.TAG, "related-item-clicked|name="+item.getName() );

					// TODO: Add to cart if item isDefined, otherwise start another
					// configure-item screen
					Cart cart = (Cart) access_.session( Conf.SESSION_CART );
					cart.add( Util.toCartCombo(item) );
				}
			});

			relatedItemsView.setLongClickable(false);
			// Fit as many columns as possible
			relatedItemsView.setColumnWidth( relatedItemsView.measureMaxChildWidth() );
		}

		return fragmentRootLayout;
	}

	private void enableAddToCart( boolean enabled )
	{
		if ( enabled )
		{
			addToCartBtn_.setVisibility( View.VISIBLE );
			addToCartBtn_.setEnabled( true );
		}
		else 
		{
			addToCartBtn_.setVisibility( View.INVISIBLE );
			addToCartBtn_.setEnabled( false );
		}
	}

	private Button addToCartBtn_;
	private SessionManager access_;
	private Handler dispatchable_;
	private Item parent_;
	private List<Item> relatedItems_;
}
