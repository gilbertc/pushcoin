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
import android.app.AlertDialog;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import java.util.ArrayList;
import android.util.Log;

public class CategoryMenuFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Log.v( Conf.TAG, "CategoryMenuFragment::onCreateView" );
		// Listview showing categories.
		View layout = inflater.inflate(R.layout.category_menu, container, false);
		ListView menu = (ListView)layout.findViewById( R.id.category_menu );
		final CategoryListAdapter model = new CategoryListAdapter(getActivity(), R.layout.category_menu_row, R.id.category_menu_label);
		menu.setAdapter(model);

		// install click-event listener
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					CategoryListAdapter.Entry entry = model.getEntry( position );
					model.setActiveEntry( position );
					EventHub.post( MessageId.CATEGORY_CLICKED, entry.cat.tagId );
				}
			});

		return layout;
	}
}
