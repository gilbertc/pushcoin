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

