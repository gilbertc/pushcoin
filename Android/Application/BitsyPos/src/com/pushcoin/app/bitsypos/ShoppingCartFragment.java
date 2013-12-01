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

import de.timroes.swipetodismiss.SwipeDismissList;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.app.Fragment;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView;
import android.util.Log;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.lang.ref.WeakReference;

public class ShoppingCartFragment extends Fragment
{
	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );

		// cache few color resources
		btnTextColorOn_ = getResources().getColor( android.R.color.white );
		btnTextColorOff_ = getResources().getColor( R.color.lightui_lightgray );
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "ShoppingCartFragment" );

		// Reset cart contents
		onCartContentChanged();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// Remove self from the event hub.
		EventHub.getInstance().unregister( handler_ );
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context ctx = getActivity();

		// Cart adapter
		adapter_ = new CartEntryArrayAdapter(ctx,
			R.layout.shopping_cart_row,
			R.id.shopping_cart_entry_title,
			R.id.shopping_cart_entry_price,
			R.id.shopping_cart_entry_note);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);

		// Cache cart-total text view
		cartTotal_ = (TextView) cartLayout.findViewById(R.id.shopping_cart_total);
		// Tab name
		tabName_ = (TextView) cartLayout.findViewById(R.id.shopping_cart_tab_name);

		// Button: Start Over
		startOverBtn_ = (Button) cartLayout.findViewById(R.id.shopping_cart_startover_button);
		Util.setCustomFont( ctx, startOverBtn_, Conf.ASSET_FONT_ICOMOON );
		startOverBtn_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Confirm user really wants to clear the cart
				ClearCartDialog.showDialog( getFragmentManager() );
			}
		});

		// Button: Open Item
		openItemBtn_ = (Button) cartLayout.findViewById(R.id.shopping_cart_open_item_button);
		Util.setCustomFont( ctx, openItemBtn_, Conf.ASSET_FONT_ICOMOON );
		openItemBtn_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEditCartItem( Conf.CART_OPEN_ITEM_ID );
			}
		});

		// Button: Queue Order
		queueOrderBtn_ = (Button) cartLayout.findViewById(R.id.shopping_cart_queue_order_button);
		Util.setCustomFont( ctx, queueOrderBtn_, Conf.ASSET_FONT_ICOMOON );

		// Button: Print Receipt
		printReceiptBtn_ = (Button) cartLayout.findViewById(R.id.shopping_cart_print_button);
		Util.setCustomFont( ctx, printReceiptBtn_, Conf.ASSET_FONT_ICOMOON );

		// Button: Checkout
		checkoutBtn_ = (Button) cartLayout.findViewById(R.id.shopping_cart_checkout_button);
		checkoutBtn_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EventHub.post( MessageId.CHECKOUT_CLICKED );
			}
		});

		ListView cartItemList = (ListView) cartLayout.findViewById(R.id.shopping_cart_list);
		// Keep in focus
		cartItemList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		cartItemList.setAdapter(adapter_);

		SwipeDismissList.OnDismissCallback callback = 
			new SwipeDismissList.OnDismissCallback() {
				public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) 
				{
					final Cart cart = CartManager.getInstance().getActiveCart();

					Log.v(Conf.TAG, "place=cart-dismiss;position="+position );
					// Get the item from the cart (before it's deleted)
					if (cart.size() > position)
					{
						final Cart.Combo deletedItem = cart.get(position);

						if ( cart.remove(position) != null ) {
						}

						// Return an Undoable implementing every method
						return new SwipeDismissList.Undoable()
							{
								// Method is called when user undoes this deletion
								public void undo() {
									cart.insert( deletedItem, position );
								}

								// Return an undo message for that item
								public String getTitle() {
									return deletedItem.getName() + " removed";
								}
							};
					}
					return null;
				}
			};

		// The constructor adds 'self' to the provided ListView (first arg)
		SwipeDismissList dismissList = new SwipeDismissList(cartItemList, callback, SwipeDismissList.UndoMode.SINGLE_UNDO);
		dismissList.setAutoHideDelay(Conf.CART_UNDO_HIDE_DELAY); 
		dismissList.setRequireTouchBeforeDismiss(false);

		// install cart-item listener
		cartItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onEditCartItem(position);
			}
		});

		return cartLayout;
	}

	private void onCartContentChanged()
	{
		// Update cart total
		Cart cart = CartManager.getInstance().getActiveCart();
		cartTotal_.setText( NumberFormat.getCurrencyInstance().format( cart.totalValue() ) );
		// cart name might have changed too -- on tab change
		tabName_.setText( CartManager.getInstance().getActive().name );

		// Turn on/off buttons depending on cart state.
		if ( cart.isEmpty() )
		{
			Util.disableButton( startOverBtn_, R.drawable.btn_gray, btnTextColorOff_);
			Util.disableButton( queueOrderBtn_, R.drawable.btn_gray, btnTextColorOff_);
			Util.disableButton( printReceiptBtn_, R.drawable.btn_gray, btnTextColorOff_);
			Util.disableButton( checkoutBtn_, R.drawable.btn_gray, btnTextColorOff_);
		}
		else // cart not empy
		{
			Util.enableButton( startOverBtn_, R.drawable.btn_red, btnTextColorOn_);
			Util.enableButton( queueOrderBtn_, R.drawable.btn_yellow, btnTextColorOn_);
			Util.enableButton( printReceiptBtn_, R.drawable.btn_purple, btnTextColorOn_);
			Util.enableButton( checkoutBtn_, R.drawable.btn_green, btnTextColorOn_);
		}

		adapter_.refreshView();
	}

	private void onEditCartItem(int position)
	{
		Log.v( Conf.TAG, "cart-item-clicked;position="+position );

		// DialogFragment.show() will add the fragmentin a transaction, 
		// but we  need to remove any currently shown dialog.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(Conf.DIALOG_EDIT_CART);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = EditCartItemFragment.newInstance(position);
		newFragment.show(ft, Conf.DIALOG_EDIT_CART);
	}

	private CartEntryArrayAdapter adapter_;
	private TextView cartTotal_;
	private TextView tabName_;
	private Handler handler_;
	private Button startOverBtn_;
	private Button openItemBtn_;
	private Button queueOrderBtn_;
	private Button printReceiptBtn_;
	private Button checkoutBtn_;

	private int btnTextColorOn_;
	private int btnTextColorOff_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<ShoppingCartFragment> ref_; 

		IncomingHandler(ShoppingCartFragment ref) {
			ref_ = new WeakReference<ShoppingCartFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			ShoppingCartFragment ref = ref_.get();
			if (ref != null)
			{
				Log.v(Conf.TAG, "ShoppingCartFragment|event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
				switch( msg.what )
				{
					case MessageId.CART_POOL_CHANGED:
					case MessageId.CART_CONTENT_CHANGED:
						ref.onCartContentChanged();
					break;
				}
			}
		}
	}
}
