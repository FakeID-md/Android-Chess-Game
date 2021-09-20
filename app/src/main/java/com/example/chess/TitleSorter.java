package com.example.chess;

import java.util.Comparator;

class TitleSorter implements Comparator<Record>
{
	@Override
	public int compare(Record o0, Record o1)
	{
		return o0.title.compareTo(o1.title);
	}
}