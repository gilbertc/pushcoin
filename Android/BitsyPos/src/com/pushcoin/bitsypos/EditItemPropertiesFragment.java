package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.app.Fragment;
import android.app.Activity;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
		try
		{
			// if we are nested or terget was set specificially, we use that
			// otherwise we relay to the activity
			Fragment target;
			if ( (target = getParentFragment()) != null ) {
				callback_ = (OnDismissed) target;
			}
			else if ( (target = getTargetFragment()) != null ) {
				callback_ = (OnDismissed) target;
			} else {
				// oh well, activity must take it
				callback_ = (OnDismissed) activity;
			}
			
		} catch (ClassCastException e) {
			throw new ClassCastException("Target must implement OnDismissed");
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

		final Item item = getArguments().getParcelable( Conf.FIELD_ITEM );

		// Inflate the layout for this fragment
		backgroundView_ = inflater.inflate(R.layout.edit_item_properties_view, container, false);

		// Derive name for this property editor from the item
		TextView title = (TextView) backgroundView_.findViewById(R.id.edit_item_properties_view_name);
		title.setText( "Customize " + item.getName() );

		// Find the listview widget so we can set its adapter
		AutofitGridView propertiesListView = (AutofitGridView) backgroundView_.findViewById(R.id.edit_item_properties_view_list);

		adapter_ = new EditItemPropertiesAdapter( context, item.getProperties() );
		propertiesListView.setAdapter( adapter_ );

		// Uncomment to fit as many columns as we can
		// propertiesListView.setColumnWidth( propertiesListView.measureMaxChildWidth() );

		// Install Done handler
		final Button doneBtn = (Button) backgroundView_.findViewById( R.id.edit_item_properties_view_done_button );
		doneBtn.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					List<Map.Entry<String,String>> list = adapter_.getProperties();
					Map<String,String> properties = new TreeMap<String,String>();
					for (Map.Entry<String,String> i : list) properties.put(i.getKey(),i.getValue());
					callback_.onEditItemPropertiesDone( item.setProperties( properties ) );
					dismiss();
				}
			});

		return backgroundView_;
	}

	private OnDismissed callback_;
	private EditItemPropertiesAdapter adapter_;

	// Main view
	View backgroundView_;
}
