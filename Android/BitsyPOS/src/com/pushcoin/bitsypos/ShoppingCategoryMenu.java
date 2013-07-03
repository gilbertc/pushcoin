package com.pushcoin.bitsypos;

import android.content.Context;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.util.AttributeSet;
import android.app.AlertDialog;

public class ShoppingCategoryMenu extends ListView
{
	private ArrayAdapter<String> test;
	String[] testItems = {"Breakfast", "Lunch", "Dinner", "Beverages"};
	 
	// If built programmatically
	public ShoppingCategoryMenu(Context context)
	{
		super(context);
		init();
	}

	// If built from XML
	public ShoppingCategoryMenu(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	 
	// If built from XML
	public ShoppingCategoryMenu(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	 
	public void init()
	{
		test = new ArrayAdapter<String>(getContext(), R.layout.shopping_category_menu_row, R.id.shopping_category_menu_label, testItems);
		setAdapter(test);
		setOnItemClickListener(new ListSelection());
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

	private class ListSelection implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage("You pressed item #" + (position+1));
			builder.setPositiveButton("OK", null);
			builder.show();
		}
	}
}

