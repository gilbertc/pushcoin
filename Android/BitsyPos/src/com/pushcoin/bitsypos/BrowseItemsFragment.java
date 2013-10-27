package com.pushcoin.bitsypos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.List;

public class BrowseItemsFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Context context = getActivity();

		// Store activity's dispacher
		dispatchable_ = ((IDispatcher)context).getDispachable();

		// What category are we displaying? 
		String categoryTag = getArguments().getString( Conf.FIELD_CATEGORY );

		// Fetch items in a given category
		items_ = AppDb.getInstance().findItemsWithTag( categoryTag, Conf.FIELD_PRICE_TAG_DEFAULT );

		if ( items_.isEmpty() ) 
		{
			// TODO: return view with message "no items were found"
			Log.v(Conf.TAG, "no-items-found|tag=breakfast");
		}

		// Inflate the layout for this fragment
		AutofitGridView view = (AutofitGridView) inflater.inflate(R.layout.browse_items_view, container, false);

		ItemSummaryArrayAdapter adapter =		
			new ItemSummaryArrayAdapter(context, R.layout.browse_items_cell, items_);

		view.setAdapter( adapter );

		// install click-event listener
		view.setOnItemClickListener(new ListSelection());

		// Fit as many columns as possible
		view.setColumnWidth( view.measureMaxChildWidth() );

		return view;
	}

	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Message m = dispatchable_.obtainMessage( MessageId.SHOPPING_ITEM_CLICKED, 0, 0, items_.get(position).getId() );
			m.sendToTarget();
		}
	}

	private Handler dispatchable_;
	private List<Item> items_;
}
