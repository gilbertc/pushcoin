package com.pushcoin.bitsypos;

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

public class ShoppingCartFragment 
	extends Fragment implements IDispatcher
{
	CartEntryArrayAdapter adapter_;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context ctx = getActivity();

		// Current cart
		final Cart cart = (Cart) SessionManager.getInstance( ctx ).session( Conf.SESSION_CART );
		adapter_ = new CartEntryArrayAdapter(ctx, cart);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);

		// Cache cart-total text view
		cartTotal_ = (TextView) cartLayout.findViewById(R.id.shopping_cart_total);

		// Handle "start over" user request
		Button startOver = (Button) cartLayout.findViewById(R.id.shopping_cart_startover_button);
		startOver.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {
				cart.clear();
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
					final Cart cart = (Cart) adapter_.getCart();

					Log.v(Conf.TAG, "place=cart-dismiss;position="+position );
					// Get the item from the cart (before it's deleted)
					if (cart.size() > position)
					{
						final Cart.Combo deletedItem = cart.get(position);

						// Delete from cart
						adapter_.remove(position);

						// Return an Undoable implementing every method
						return new SwipeDismissList.Undoable() {

								// Method is called when user undoes this deletion
								public void undo() {
										// Reinsert item to list
										adapter_.insert(deletedItem, position);
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

		// install click-event listener
		cartItemList.setOnItemClickListener(new CartItemClickListener());

		// Reset cart contents
		onCartContentChanged();

		return cartLayout;
	}

	/** Used by fragments to access our dispatcher. */
	@Override
	public Handler getDispachable() 
	{
		return handler_;
	}
	
	/** Dispatch events */
	private Handler handler_ = new Handler() {
		@Override
		public void handleMessage(Message msg) 
		{
			Log.v(Conf.TAG, "place=cart-frag;event="+msg.what + ";arg1="+msg.arg1 + ";arg2="+msg.arg2 );
			switch( msg.what )
			{
				case MessageId.CART_CONTENT_CHANGED:
					onCartContentChanged();
				break;
			}
		}
	};

	private void onCartContentChanged()
	{
		// Update cart total
		final Cart cart = (Cart) adapter_.getCart();
		cartTotal_.setText( NumberFormat.getCurrencyInstance().format( cart.totalValue() ) );

		adapter_.refreshView();
	}

	private class CartItemClickListener implements AdapterView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Log.v( Conf.TAG, "cart-item-clicked;position="+position+";id="+id );

			// DialogFragment.show() will add the fragmentin a transaction, 
			// but we  need to remove any currently shown dialog.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag(Conf.DIALOG_EDIT_CART_ID);
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the dialog.
			DialogFragment newFragment = EditCartItemFragment.newInstance(position);
			newFragment.show(ft, Conf.DIALOG_EDIT_CART_ID);
		}
	}

	private TextView cartTotal_;
}
