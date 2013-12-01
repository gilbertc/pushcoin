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

package com.pushcoin.icebreaker;

import android.content.Context;
import android.widget.ListView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
	Author: Jason
	http://jasonfry.co.uk/blog/android-overscroll-revisited/
*/
public class BounceListView extends ListView
{
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;
    
	private Context mContext;
	private int mMaxYOverscrollDistance;
	
	public BounceListView(Context context) 
	{
		super(context);
		mContext = context;
		initBounceListView();
	}
	
	public BounceListView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = context;
		initBounceListView();
	}
	
	public BounceListView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		mContext = context;
		initBounceListView();
	}
	
	private void initBounceListView()
	{
		//get the density of the screen and do some maths with it on the max overscroll distance
		//variable so that you get similar behaviors no matter what the screen size
		
		final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        	final float density = metrics.density;
        
		mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
	}
	
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) 
	{ 
		//This is where the magic happens, we have replaced the incoming maxOverScrollY with our own custom variable mMaxYOverscrollDistance; 
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);  
	}
	
}
