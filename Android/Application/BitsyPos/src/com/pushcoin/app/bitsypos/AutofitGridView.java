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

import android.content.Context;
import android.widget.GridView;
import android.view.View;
import android.util.AttributeSet;

public class AutofitGridView extends GridView
{
	// If built programmatically
	public AutofitGridView(Context context)
	{
		super(context);
	}

	// If built from XML
	public AutofitGridView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	 
	// If built from XML
	public AutofitGridView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	 
	public int measureMaxChildWidth() 
	{
		int maxWidth = 0;
		View view = null;
		for (int i = 0, count = getAdapter().getCount(); i < count; i++) 
		{
			view = getAdapter().getView(i, view, this);
			view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			if (view.getMeasuredWidth() > maxWidth) {
				maxWidth = view.getMeasuredWidth();
			}
		}
		return maxWidth;
	}
}

