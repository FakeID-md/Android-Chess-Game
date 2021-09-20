package com.example.chess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.widget.Toast;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class Pawn extends Piece
{
	public Pawn(Context context, int Side, com.example.chess.Kind Kind, IGame Delegate)
	{
		super(context, Side, Kind, Delegate);
	}

	public ArrayList<Point> GetLegalMoves()
	{
		if (Kind == com.example.chess.Kind.Queen)
			return super.GetLegalMoves();

		ArrayList<Point> Moves = new ArrayList<>();

		int DestRow = row + side;

		if (DestRow < 8 && DestRow >= 0)
		{
			Piece AliveOccupant = GetAliveOccupant(col, DestRow);

			if (AliveOccupant == null)
			{
				Moves.add(new Point(col, DestRow));
			}
		}

		if (!Moved && Moves.size() != 0)
		{
			DestRow = row + 2 * side;

			if (DestRow < 8 && DestRow >= 0)
			{
				Piece AliveOccupant = GetAliveOccupant(col, DestRow);

				if (AliveOccupant == null)
				{
					Moves.add(new Point(col, DestRow));
				}
			}
		}

		int DestCol = col + 1;
		DestRow = row + side;

		if (DestCol <8 && DestCol >= 0 && DestRow < 8 && DestRow >= 0)
		{
			Piece AliveOccupant = GetAliveOccupant(DestCol, DestRow);

			if (AliveOccupant != null && AliveOccupant.side != side)
			{
				Moves.add(new Point(DestCol, DestRow));
			}
		}

		DestCol = col - 1;
		DestRow = row + side;

		if (DestCol <8 && DestCol >= 0 && DestRow < 8 && DestRow >= 0)
		{
			Piece AliveOccupant = GetAliveOccupant(DestCol, DestRow);

			if (AliveOccupant != null && AliveOccupant.side != side)
			{
				Moves.add(new Point(DestCol, DestRow));
			}
		}

		return Moves;
	}

	public void MoveTo(int col, int row)
	{
		super.MoveTo(col, row);

		if (Kind == com.example.chess.Kind.Queen)
			return;

		if (row == (side == 1 ? 7 : 0))
		{
			LegalMoves.clear();

			LegalMoves.add(new Point(1, 0));
			LegalMoves.add(new Point(-1, 0));
			LegalMoves.add(new Point(0, -1));
			LegalMoves.add(new Point(0, 1));

			LegalMoves.add(new Point(1, 1));
			LegalMoves.add(new Point(-1, 1));
			LegalMoves.add(new Point(1, -1));
			LegalMoves.add(new Point(-1, -1));

			ConsecutiveMove = true;

			setBackground(getResources().getDrawable(side > 0 ? R.drawable.queen0 : R.drawable.queen1, null));

			Kind = com.example.chess.Kind.Queen;

			Toast.makeText(getContext(), "Congratulations, your Pawn becomes Queen.", Toast.LENGTH_LONG).show();
		}
	}
}