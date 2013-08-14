package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import de.timroes.swipetodismiss.SwipeDismissList;
import java.util.ArrayList;
import android.util.Log;

public class ShoppingCartFragment 
	extends Fragment implements IDispatcher
{
	CartEntryArrayAdapter adapter_;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context ctx = getActivity();

		// Current cart
		Cart cart = (Cart) SessionManager.getInstance( ctx ).session( Conf.SESSION_CART );
		adapter_ = new CartEntryArrayAdapter(ctx, R.layout.shopping_cart_row, R.id.shopping_cart_entry_title, R.id.shopping_cart_entry_price, cart);

		// Inflate the layout for this fragment
		View cartLayout = inflater.inflate(R.layout.shopping_cart, container, false);
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
						final Item deletedItem = cart.get(position);

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
					onCartContentChanged( msg );
				break;
			}
		}
	};

	private void onCartContentChanged( Message msg )
	{
		adapter_.refreshView();
	}
}