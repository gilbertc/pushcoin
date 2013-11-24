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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Fragment;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.content.Context;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;
import java.text.NumberFormat;
import java.math.BigDecimal;

public class EditCartItemFragment extends DialogFragment
{
	/**
		Create a new instance, providing cart item ID. 
	*/
	static EditCartItemFragment newInstance(int position)
	{
		EditCartItemFragment f = new EditCartItemFragment();

		// Supply position input as an argument.
		Bundle args = new Bundle();
		args.putInt(Conf.FIELD_CART_ITEM_POSITION, position);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog_NoActionBar);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context context = getActivity();

		cartItemId_ = getArguments().getInt( Conf.FIELD_CART_ITEM_POSITION );

		// Obtain current cart
		Cart cart = CartManager.getInstance().getActiveCart();

		// Are we adding a new item or modifying existing one?
		if ( cartItemId_ == Conf.CART_OPEN_ITEM_ID )
		{
			combo_ = new Cart.Combo();
			combo_.entries.add( 
				new Cart.Entry( Conf.CART_ITEM_EMPTY_SKU, Conf.CART_ITEM_EMPTY_NAME, 1, new BigDecimal(0) ) );
		}
		else 
		{
			// Locate cart item we are modifying and make a clone of it
			combo_ = new Cart.Combo( cart.get( cartItemId_ ) );
		}

		// Inflate the layout for this fragment
		backgroundView_ = inflater.inflate(R.layout.edit_cart_item_view, container, false);

		// References to widgets with changing values
		specialInstructions_ = (EditText) backgroundView_.findViewById(R.id.edit_cart_item_view_special_instructions);
		comboName_ = (TextView) backgroundView_.findViewById(R.id.edit_cart_item_view_name);
		basePrice_ = (EditText) backgroundView_.findViewById(R.id.edit_cart_item_view_baseprice);
		totalPrice_ = (TextView) backgroundView_.findViewById(R.id.edit_cart_item_view_comboprice);

		// Find the listview widget so we can set its adapter
		ListView itemsListView = (ListView) backgroundView_.findViewById(R.id.edit_cart_item_view_list);
		itemsListView.addHeaderView( inflater.inflate(R.layout.edit_cart_item_row_header, null) );

		listViewAdapter_ = new EditCartItemArrayAdapter( 
			context, 
			new EditCartItemArrayAdapter.OnContentChanged() { 
				public void onChanged() {
					onComboModified(false);
				}

				// Flash background on error
				public void onInputError() {
					onBadInput();
				}
			}, combo_ );
		itemsListView.setAdapter( listViewAdapter_ );

		// Install Cancel handler
		final Button closeBtn = (Button) backgroundView_.findViewById( R.id.edit_cart_item_view_cancel_button );
		closeBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {
				dismiss();
			}
		});

		// Install Save handler
		final Button saveBtn = (Button) backgroundView_.findViewById( R.id.edit_cart_item_view_save_button );
		saveBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				// make sure changes in other edit-views are persisted on their 'lost focus'
				backgroundView_.requestFocus();
				saveChanges();
				scheduleDismiss();
			}
		});

		// Recalculate totals if base price changes
		basePrice_.setOnFocusChangeListener( new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus)
			{
				EditText field = (EditText) v;
				if (!hasFocus)
				{
					// Update only if changed
					String newVal = field.getText().toString();
					if ( !newVal.isEmpty() && !newVal.equals( combo_.basePrice.toString()) ) 
					{
						try
						{
							combo_.basePrice = new BigDecimal( newVal );
							onComboModified(false);
						} catch (NumberFormatException e) { 
							onBadInput();
						}
					} 
					field.setText( NumberFormat.getCurrencyInstance().format(combo_.basePrice) );
				} 
				else {
					clearFieldShowKeyboard( field );
				}
			}
		});

		// this prevents the field from regaining focus after it's edited
		basePrice_.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if ( actionId == EditorInfo.IME_ACTION_DONE )
				{
					v.clearFocus();
					onComboModified(false);
				}
				return false;
			}
		});

		// Update special instructions
		specialInstructions_.setOnFocusChangeListener( new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus)
			{
				EditText field = (EditText) v;
				if (!hasFocus)
				{
					// Update only if changed
					String newVal = field.getText().toString();
					if ( !newVal.equals(combo_.note) ) 
					{
						combo_.note = newVal;
						onComboModified(false);
					} 
				} 
			}
		});

		// this prevents the field from regaining focus after it's edited
		specialInstructions_.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if ( actionId == EditorInfo.IME_ACTION_DONE )
				{
					v.clearFocus();
					onComboModified(false);
				}
				return false;
			}
		});

		// Set initial values
		onComboModified(true);

		return backgroundView_;
	}

	private void clearFieldShowKeyboard(EditText v)
	{
		v.setText("");
		v.requestFocus();
		InputMethodManager manager= (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
	}

	private void scheduleDismiss()
	{
		queue_.postDelayed(new Runnable() {
			public void run() {
				dismiss();
			}
		}, 100);
	}

	// Recalculates totals, sets combo name, etc
	private void onComboModified( boolean init )
	{
		comboName_.setText( combo_.getName() );
		specialInstructions_.setText( combo_.note );
		totalPrice_.setText( combo_.getPrettyPrice() );
		if (init) {
			basePrice_.setText( NumberFormat.getCurrencyInstance().format( combo_.basePrice ) );
		}

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(totalPrice_.getWindowToken(), 0);
		totalPrice_.requestFocus();
	}

	private void onBadInput()
	{
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(50); 
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(1);
		backgroundView_.startAnimation(anim);
	}

	// Persists combo changes in the cart.
	private void saveChanges() {
		CartManager.getInstance().getActiveCart().replace( combo_, cartItemId_ );
	}

	private android.os.Handler queue_ = new android.os.Handler();

	// Model
	private int cartItemId_;
	private Cart.Combo combo_;

	// Main view
	View backgroundView_;

	// View widgets
	EditText specialInstructions_;
	TextView comboName_;
	EditText basePrice_;
	TextView totalPrice_;
	EditCartItemArrayAdapter listViewAdapter_;
}
