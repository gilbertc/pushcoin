package com.pushcoin.bitsypos;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.DialogFragment;
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

public class ConfigureItemFragment
	extends Fragment implements EditItemPropertiesFragment.OnDismissed
{

	/**
		Create a new instance, providing item.
	*/
	static ConfigureItemFragment newInstance( Item item )
	{
		ConfigureItemFragment f = new ConfigureItemFragment();

		// Supply item we are configuring
		Bundle args = new Bundle();
		args.putParcelable(Conf.FIELD_ITEM, item);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		final Context ctx = getActivity();

		// Unpack item to configure
		parent_ = getArguments().getParcelable( Conf.FIELD_ITEM );

		// Inflate the layout for this fragment
		View fragmentRootLayout = inflater.inflate(R.layout.configure_item_view, container, false);

		// Set combo name
		addToCartBtn_ = (Button) fragmentRootLayout.findViewById( R.id.add_combo_item_to_cart );
		addToCartBtn_.setText( "Add " + parent_.getName() );
		// on click, add item to cart
		addToCartBtn_.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {
				Cart cart = (Cart) SessionManager.getInstance().get( Conf.SESSION_KEY_CART );
				cart.add( Util.toCartCombo(parent_) );
			}
		});

		// Create a list-view for each slot being configured
		LinearLayout layoutSlots = (LinearLayout) fragmentRootLayout.findViewById( R.id.item_configuration_arena );

		List<Item> children = parent_.getChildren();
		for ( int i = 0; i < children.size(); ++i )
		{
			final Item slot = children.get(i); 
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
			final int slotIndx = i; 
			listview.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						Item chosenItem = slot.getAlternatives().get( position );
						Log.v(Conf.TAG, "slot-item-clicked|slot="+slot.getName()+";name="+chosenItem.getName()+";position="+position );
						curSlotIndx_ = slotIndx;
						curSlotTitle_ = title;

						// User can change item properties before adding it to slot
						if ( chosenItem.hasProperties() ) 
						{
							// DialogFragment.show() will add the fragmentin a transaction, 
							// but we  need to remove any currently shown dialog.
							FragmentTransaction ft = getFragmentManager().beginTransaction();
							Fragment prev = getFragmentManager().findFragmentByTag( Conf.DIALOG_EDIT_ITEM_PROPERTIES );
							if (prev != null) {
								ft.remove(prev);
							}
							ft.addToBackStack(null);

							// Create and show the dialog.
							DialogFragment newFragment = EditItemPropertiesFragment.newInstance( chosenItem );
							// Set fragment as the recipient of events
							newFragment.setTargetFragment( ConfigureItemFragment.this, 0 );
							newFragment.show(ft, Conf.DIALOG_EDIT_ITEM_PROPERTIES);
						}
						else {
							putItemInSlot( chosenItem );
						}
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
						Cart cart = (Cart) SessionManager.getInstance().get( Conf.SESSION_KEY_CART );
						cart.add( Util.toCartCombo(item) );
					}
				});

			relatedItemsView.setLongClickable(false);
			// Fit as many columns as possible
			relatedItemsView.setColumnWidth( relatedItemsView.measureMaxChildWidth() );
		}

		return fragmentRootLayout;
	}

	private void putItemInSlot( Item chosenItem )
	{
		if (curSlotTitle_ == null) {
			Log.e(Conf.TAG, "Cannot modify slot"); return;
		}
		// TODO: what if still not defined? this will throw, so create 
		// another configure-screen - go recursively until all levels are defined
		parent_ = parent_.replace( curSlotIndx_, chosenItem );
		curSlotTitle_.setText( chosenItem.getName() );
		curSlotTitle_.setTextColor( getActivity().getResources().getColor(R.color.DarkBlue) );
		// restore defaults
		curSlotIndx_ = 0; curSlotTitle_ = null;

		// Re-evalute if all slots are configured and user can add item to cart.
		enableAddToCart( parent_.isDefined() );
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

	@Override
	public void onEditItemPropertiesDone( Item chosenItem ) {
		putItemInSlot( chosenItem );
	}

	private Button addToCartBtn_;
	private Item parent_;
	private List<Item> relatedItems_;

	private int curSlotIndx_ = 0;
	private TextView curSlotTitle_ = null;
}
