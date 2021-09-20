package com.example.chess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class Piece extends AppCompatButton implements IGame
{
	int side;
	com.example.chess.Kind Kind;

	int col;
	int row;

	boolean Moved;
	boolean Swapped;
	boolean captured;
	boolean ConsecutiveMove;

	ArrayList<Point> LegalMoves = new ArrayList<>();

	private final IGame delegate;

	@SuppressLint("UseCompatLoadingForDrawables")
	public Piece(Context context, int Side, com.example.chess.Kind Kind, IGame Delegate)
	{
		super(context);
		setId(generateViewId());

		this.delegate = Delegate;

		this.side = Side;
		this.Kind = Kind;

		switch (Kind)
		{
			case Pawn:
			{
				LegalMoves.add(new Point(0, 1));
				ConsecutiveMove = true;

				setBackground(getResources().getDrawable(Side > 0 ? R.drawable.pawn0 : R.drawable.pawn1, null));
				break;
			}

			case Rook:
			{
				LegalMoves.add(new Point(1, 0));
				LegalMoves.add(new Point(-1, 0));
				LegalMoves.add(new Point(0, -1));
				LegalMoves.add(new Point(0, 1));

				ConsecutiveMove = true;

				setBackground(getResources().getDrawable(Side > 0 ? R.drawable.rook0 : R.drawable.rook1, null));
				break;
			}

			case Knight:
			{
				LegalMoves.add(new Point(-2, 1));
				LegalMoves.add(new Point(2, 1));
				LegalMoves.add(new Point(-1, 2));
				LegalMoves.add(new Point(1, 2));

				LegalMoves.add(new Point(-2, -1));
				LegalMoves.add(new Point(2, -1));
				LegalMoves.add(new Point(-1, -2));
				LegalMoves.add(new Point(1, -2));

				ConsecutiveMove = false;

				setBackground(getResources().getDrawable(Side > 0 ? R.drawable.knight0 : R.drawable.knight1, null));
				break;
			}

			case Bishop:
			{
				LegalMoves.add(new Point(1, 1));
				LegalMoves.add(new Point(-1, 1));
				LegalMoves.add(new Point(1, -1));
				LegalMoves.add(new Point(-1, -1));

				ConsecutiveMove = true;

				setBackground(getResources().getDrawable(side > 0 ? R.drawable.bishop0 : R.drawable.bishop1, null));
				break;
			}

			case Queen:
			{
				LegalMoves.add(new Point(1, 0));
				LegalMoves.add(new Point(-1, 0));
				LegalMoves.add(new Point(0, -1));
				LegalMoves.add(new Point(0, 1));

				LegalMoves.add(new Point(1, 1));
				LegalMoves.add(new Point(-1, 1));
				LegalMoves.add(new Point(1, -1));
				LegalMoves.add(new Point(-1, -1));

				ConsecutiveMove = true;

				setBackground(getResources().getDrawable(Side > 0 ? R.drawable.queen0 : R.drawable.queen1, null));
				break;
			}

			case King:
			{
				LegalMoves.add(new Point(1, 0));
				LegalMoves.add(new Point(-1, 0));
				LegalMoves.add(new Point(0, -1));
				LegalMoves.add(new Point(0, 1));

				LegalMoves.add(new Point(1, 1));
				LegalMoves.add(new Point(-1, 1));
				LegalMoves.add(new Point(1, -1));
				LegalMoves.add(new Point(-1, -1));

				ConsecutiveMove = false;

				setBackground(getResources().getDrawable(Side > 0 ? R.drawable.king0 : R.drawable.king1, null));
				break;
			}

			default:
				break;
		}

		Moved = false;
		captured = false;
		Swapped = false;
	}

	public void InitLocation(int col, int row)
	{
		MoveTo(col, row);
		Moved = false;
	}

	public void MoveTo(int col, int row)
	{
		this.col = col;
		this.row = row;

		int DestId = GetGridID(col, row);

		ConstraintLayout kLayout = GetLayout();

		ConstraintSet set = new ConstraintSet();
		set.clone(kLayout);

		set.connect(getId(), ConstraintSet.LEFT, DestId, ConstraintSet.LEFT);
		set.connect(getId(), ConstraintSet.TOP, DestId, ConstraintSet.TOP);

		set.applyTo(kLayout);

		Moved = true;
	}

	public ArrayList<Point> GetLegalMoves()
	{
		ArrayList<Point> Moves = new ArrayList<>();

		if (captured)
			return Moves;

		for (int i = 0; i < LegalMoves.size(); i++)
		{
			for (int j = 1; j <= (ConsecutiveMove ? 8 : 1); j++)
			{
				int DestCol = col + LegalMoves.get(i).x * j;
				int DestRow = row + LegalMoves.get(i).y * j * side;

				if (DestCol >= 8 || DestCol < 0 || DestRow >= 8 || DestRow < 0)
				{
					break;
				}

				Piece Occupant = GetAliveOccupant(DestCol, DestRow);

				if (Occupant == null)
				{
					Moves.add(new Point(DestCol, DestRow));
				}
				else
				{
					if (Occupant.side != side)
					{
						Moves.add(new Point(DestCol, DestRow));
					}

					break;
				}
			}
		}

		return Moves;
	}

	public ArrayList<Point> GetRealLegalMoves()
	{
		ArrayList<Point> LegalMoves = GetLegalMoves();
		ArrayList<Point> RealLegalMoves = new ArrayList<>();

		int OriginX = col;
		int OriginY = row;

		for (int i = 0; i < LegalMoves.size(); i++)
		{
			Piece AliveOpponent = GetAliveOccupant(LegalMoves.get(i).x, LegalMoves.get(i).y);

			col = LegalMoves.get(i).x;
			row = LegalMoves.get(i).y;

			if (AliveOpponent == null)
			{
				if (!IsChecked(side))
				{
					RealLegalMoves.add(LegalMoves.get(i));
				}
			}
			else
			{
				AliveOpponent.captured = true;

				if (!IsChecked(side))
				{
					RealLegalMoves.add(LegalMoves.get(i));
				}

				AliveOpponent.captured = false;
			}
		}

		col = OriginX;
		row = OriginY;

		return RealLegalMoves;
	}

	@Override
	public boolean IsChecked(int side)
	{
		return delegate.IsChecked(side);
	}

	@Override
	public Piece GetAliveOccupant(int col, int row)
	{
		return delegate.GetAliveOccupant(col, row);
	}

	@Override
	public int GetGridID(int col, int row)
	{
		return delegate.GetGridID(col, row);
	}

	@Override
	public ConstraintLayout GetLayout()
	{
		return delegate.GetLayout();
	}
}