package com.example.chess;

import java.util.Comparator;

class DatetimeSorter implements Comparator<Record>
{
	@Override
	public int compare(Record o0, Record o1)
	{
		return Integer.compare(o0.ID, o1.ID);
	}
}