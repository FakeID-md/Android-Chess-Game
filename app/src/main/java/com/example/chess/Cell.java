package com.example.chess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

@SuppressLint("ViewConstructor")
public class Cell extends View
{
	public int col;
	public int row;

	public Cell(Context context, int id)
	{
		super(context);
		setId(generateViewId());

		this.col = id % 8;
		this.row = id / 8;

		setBackgroundColor((row + col) % 2 == 0 ? Colors.DARK : Colors.LIGHT);
	}

	public void reset()
	{
		setBackgroundColor((row + col) % 2 == 0 ? Colors.DARK : Colors.LIGHT);
	}

	public void select(int fill)
	{
		int strokeWidth = 1;
		int roundRadius = 15;
		int strokeColor = Colors.Border;

		GradientDrawable gd = new GradientDrawable();
		gd.setColor(fill);
		gd.setCornerRadius(roundRadius);
		gd.setStroke(strokeWidth, strokeColor);
		setBackground(gd);
	}
}