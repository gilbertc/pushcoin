package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Fragment;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.GridView;
import android.database.DataSetObserver;
import java.text.NumberFormat;
import android.text.InputType;
import java.util.ArrayList;
import java.math.BigDecimal;

public class EditItemPropertiesFragment extends DialogFragment
{
	/**
		Activity hosting this fragment must implement this 
		event-handler interface.
	*/
	public interface OnDismissed
	{
		void onEditItemPropertiesDone( Item item );
	}

	/**
		Create a new instance, providing item ID. 
	*/
	static EditItemPropertiesFragment newInstance( Item item )
	{
		EditItemPropertiesFragment f = new EditItemPropertiesFragment();

		// Supply position input as an argument.
		Bundle args = new Bundle();
		args.putParcelable(Conf.FIELD_ITEM, item);
		f.setArguments(args);

		return f;
	}

	/**
		Registers a handler for events coming from this fragment.
	*/
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try {
			callback_ = (OnDismissed) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnDismissed");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context context = getActivity();

		item_ = getArguments().getParcelable( Conf.FIELD_ITEM );

		// Obtain access to session manager, from which we get current cart
		session_ = SessionManager.getInstance( context );

		// Inflate the layout for this fragment
		backgroundView_ = inflater.inflate(R.layout.edit_item_properties_view, container, false);

		// Derive name for this property editor from the item
		TextView title = (TextView) backgroundView_.findViewById(R.id.edit_item_properties_view_name);
		title.setText( "Customize " + item_.getName() );

		// Find the listview widget so we can set its adapter
		AutofitGridView propertiesListView = (AutofitGridView) backgroundView_.findViewById(R.id.edit_item_properties_view_list);

		EditItemPropertiesAdapter adapter =		
			new EditItemPropertiesAdapter( context, item_.getProperties() );
		propertiesListView.setAdapter( adapter );

		// Uncomment to fit as many columns as we can
		// propertiesListView.setColumnWidth( propertiesListView.measureMaxChildWidth() );

		// Install Done handler
		final Button doneBtn = (Button) backgroundView_.findViewById( R.id.edit_item_properties_view_done_button );
		doneBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				callback_.onEditItemPropertiesDone( item_ );
				dismiss();
			}
		});

		return backgroundView_;
	}

	private SessionManager session_;
	private OnDismissed callback_;
	private Item item_;

	// Main view
	View backgroundView_;
}
