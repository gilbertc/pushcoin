/*
 * Based on work by Mobs and Geeks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file 
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the 
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.pushcoin.app.bitsypos;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.mobsandgeeks.ui.R.styleable;

import java.util.HashMap;
import java.util.Map;

/**
 * Subclass of {@link EditText} that supports the <code>customTypeface</code> attribute from XML.
 *
 * @author Ragunath Jawahar <rj@mobsandgeeks.com>
 */
public class TypefaceEditText extends EditText
{
	/*
	 * Caches typefaces based on their file path and name, so that they don't have to be created
	 * every time when they are referenced.
	 */
	private static Map<String, Typeface> mTypefaces;

	public TypefaceEditText(final Context context)
	{
		super(context);
		initTypeface(context, null);
	}

	public TypefaceEditText(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		initTypeface(context, attrs);
	}

	public TypefaceEditText(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		initTypeface(context, attrs);
	}

	private void initTypeface(final Context context, final AttributeSet attrs)
	{
		if ( isInEditMode() ) {
			return;
		}

		if (mTypefaces == null) {
			mTypefaces = new HashMap<String, Typeface>();
		}

		final TypedArray array = context.obtainStyledAttributes(attrs, styleable.TypefaceTextView);
		if (array != null)
		{
			final String typefaceAssetPath = array.getString( R.styleable.TypefaceTextView_customTypeface );

			if (typefaceAssetPath != null)
			{
				Typeface typeface = null;

				if (mTypefaces.containsKey(typefaceAssetPath)) {
					typeface = mTypefaces.get(typefaceAssetPath);
				} 
				else 
				{
					AssetManager assets = context.getAssets();
					typeface = Typeface.createFromAsset(assets, typefaceAssetPath);
					mTypefaces.put(typefaceAssetPath, typeface);
				}

				setTypeface(typeface);
			}
			array.recycle();
		}
	}
}
