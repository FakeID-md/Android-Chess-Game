package com.example.chess;

import androidx.constraintlayout.widget.ConstraintLayout;

public interface IGame
{
	Piece GetAliveOccupant(int col, int row);
	int GetGridID(int col, int row);
	boolean IsChecked(int side);
	ConstraintLayout GetLayout();
}