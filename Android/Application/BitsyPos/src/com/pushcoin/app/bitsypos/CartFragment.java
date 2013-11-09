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

public class CartFragment extends Fragment
{
	CartEntryArrayAdapter adapter_;

	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// Handler where we dispatch events.
		handler_ = new IncomingHandler( this );
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume()
	{
		super.onResume();
		// Register self with the hub and start receiving events
		EventHub.getInstance().register( handler_, "CartFragment" );
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
		adapter_ = new CartEntryArrayAdapter(ctx);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);

		// Cache cart-total text view
		cartTotal_ = (TextView) cartLayout.findViewById(R.id.shopping_cart_total);
		// Tab name
		tabName_ = (TextView) cartLayout.findViewById(R.id.shopping_cart_tab_name);

		// Handle "start over" user request
		Button startOver = (Button) cartLayout.findViewById(R.id.shopping_cart_startover_button);
		startOver.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Confirm user really wants to clear the cart
				ClearCartDialogFragment.showDialog( getFragmentManager() );
			}
		});

		// Handle Add Open Item request
		Button openItemBtn = (Button) cartLayout.findViewById(R.id.shopping_cart_open_item_button);
		openItemBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEditCartItem( Conf.CART_OPEN_ITEM_ID );
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

		// Reset cart contents
		onCartContentChanged();

		return cartLayout;
	}

	private void onCartContentChanged()
	{
		// Update cart total
		Cart cart = CartManager.getInstance().getActiveCart();
		cartTotal_.setText( NumberFormat.getCurrencyInstance().format( cart.totalValue() ) );
		// cart name might have changed too -- on tab change
		tabName_.setText( CartManager.getInstance().getActiveEntry().name );

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

	private TextView cartTotal_;
	private TextView tabName_;
	private Handler handler_;

	/**
		Static handler keeps lint happy about (temporary?) memory leaks if queued 
		messages refer to the Activity (our event consumer), which now cannot
		be collected.
	*/
	static class IncomingHandler extends Handler
	{
		private final WeakReference<CartFragment> ref_; 

		IncomingHandler(CartFragment ref) {
			ref_ = new WeakReference<CartFragment>(ref);
		}

		/** Dispatch events. */
		@Override
		public void handleMessage(Message msg)
		{
			CartFragment ref = ref_.get();
			if (ref != null)
			{
				Log.v(Conf.TAG, "CartFragment|event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
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
