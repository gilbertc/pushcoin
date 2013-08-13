package com.pushcoin.bitsypos;

import android.content.Context;
import android.widget.ListView;
import android.view.View;
import android.util.AttributeSet;

public class WrapContentListView extends ListView
{
	private IconLabelArrayAdapter menu_;
	 
	// If built programmatically
	public WrapContentListView(Context context)
	{
		super(context);
	}

	// If built from XML
	public WrapContentListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	 
	// If built from XML
	public WrapContentListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	 
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int maxWidth = measureWidthByChildren() + getPaddingLeft() + getPaddingRight();
    super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);     
	}

	public int measureWidthByChildren() 
	{
		int maxWidth = 0;
		View view = null;
		for (int i = 0, count = getAdapter().getCount(); i < count; i++) 
		{
			view = getAdapter().getView(i, view, this);
			view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			if (view.getMeasuredWidth() > maxWidth)
			{
				maxWidth = view.getMeasuredWidth();
			}
		}
		return maxWidth;
	}
}

