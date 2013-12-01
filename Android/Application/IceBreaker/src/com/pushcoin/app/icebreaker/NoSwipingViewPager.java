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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.support.v4.view.ViewPager;

public class NoSwipingViewPager extends ViewPager 
{
	private boolean enabled_;

	public NoSwipingViewPager(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		enabled_ = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (enabled_) {
			return super.onTouchEvent(event);
		}
		return false;
	}

	public void setSwipingEnabled(boolean enabled)
	{
		enabled_ = enabled;
	}
}
