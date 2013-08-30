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
