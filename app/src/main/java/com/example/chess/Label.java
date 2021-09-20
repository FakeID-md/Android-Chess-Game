package com.example.chess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

@SuppressLint("ViewConstructor")
public class Label extends AppCompatTextView
{
	public Label(Context context, int ID, boolean IsRow)
	{
		super(context);
		setId(generateViewId());

		setTypeface(null, Typeface.BOLD);
		setGravity(Gravity.CENTER);

		if (IsRow)
		{
			setText(String.valueOf(ID % 8));
		}
		else
		{
			setText(String.valueOf((char)('A' + ID % 8)));
		}
	}
}