package com.example.chess;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ChessActivity extends AppCompatActivity implements IGame
{
	private ConstraintLayout layout;
	private AppCompatButton resign;
	private AppCompatButton draw;
	private AppCompatButton ai;
	private AppCompatButton undo;
	private AppCompatButton next;
	private AppCompatButton hidden;
	private Database db;
	private final ArrayList<TextView> labels = new ArrayList<>();
	private final ArrayList<Cell> grids = new ArrayList<>();
	private final ArrayList<Piece> blackPieces = new ArrayList<>();
	private final ArrayList<Piece> whitePieces = new ArrayList<>();
	private ArrayList<Point> validMoves = new ArrayList<>();
	private Piece selectedPiece = null;
	private int gridSide;
	private int inTurn = 1;
	private boolean bPlayingBack = false;
	private Piece piece;
	private Point location;
	private boolean hasMoved;
	private Kind kind;
	private final ArrayList<Point> moves = new ArrayList<>();
	private Piece targetPiece;
	private Record record;
	private final ArrayList<View> views = new ArrayList<>();
	private final UIHandler mHandler = new UIHandler(this);
	private int index = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chess);

		layout = findViewById(R.id.SuperLayout);

		resign = findViewById(R.id.resign);
		draw = findViewById(R.id.draw);
		ai = findViewById(R.id.ai);
		undo = findViewById(R.id.undo);
		next = findViewById(R.id.next);
		hidden = findViewById(R.id.hidden);
		index = 0;

		db = new Database(this);

		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		gridSide = (dm.widthPixels - 60 * 2) / 8;

		record = new Record();

		Intent intent = getIntent();

		bPlayingBack = intent.getBooleanExtra("IsPlayback", true);

		if (bPlayingBack)
		{
			record.SetSteps(intent.getStringExtra("Steps"));
			record.SetOutcome(intent.getIntExtra("Outcome", Result.YellowMates.ordinal()));
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		setLabels();
		setCells();
		setPieces();

		if (bPlayingBack)
		{
			resign.setEnabled(false);
			draw.setEnabled(false);
			ai.setEnabled(false);
			undo.setEnabled(false);
			next.setEnabled(true);

			Playback();
		}
	}

	public void onNext(View view)
	{
		if (++index == record.steps.size())
			next.setEnabled(false);
	}

	private void Playback()
	{
		new Playback().start();
	}

	private class Playback extends Thread
	{
		public void run()
		{
			super.run();

			for (int i = 0; i < record.steps.size(); i++)
			{
				while (true)
				{
					if (index > i)
						break;
				}

				int col = record.steps.get(i).get(0).x;
				int row = record.steps.get(i).get(0).y;

				Piece occupant = GetAliveOccupant(col, row);

				Message message = new Message();
				message.what = occupant != null ? occupant.getId() : grids.get(row * 8 + col).getId();
				mHandler.sendMessage(message);

				SystemClock.sleep(1000);

				col = record.steps.get(i).get(1).x;
				row = record.steps.get(i).get(1).y;

				occupant = GetAliveOccupant(col, row);

				message = new Message();
				message.what = occupant != null ? occupant.getId() : grids.get(row * 8 + col).getId();
				mHandler.sendMessage(message);

				SystemClock.sleep(1000);
			}

			Message message = new Message();
			message.what = hidden.getId();
			mHandler.sendMessage(message);
		}
	}

	private final View.OnClickListener OnGrid = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			Cell grid = (Cell) view;

			if (selectedPiece != null && isValild(grid.col, grid.row))
			{
				if (!bPlayingBack)
				{
					ArrayList<Point> thisStep = new ArrayList<>();
					thisStep.add(new Point(selectedPiece.col, selectedPiece.row));
					thisStep.add(new Point(grid.col, grid.row));
					record.steps.add(thisStep);
				}

				piece = selectedPiece;
				location = new Point(piece.col, piece.row);
				hasMoved = selectedPiece.Moved;
				kind = selectedPiece.Kind;
				moves.clear();
				moves.addAll(selectedPiece.LegalMoves);

				targetPiece = null;

				if (!bPlayingBack)
					undo.setEnabled(true);

				Recover();
				selectedPiece.MoveTo(grid.col, grid.row);

				if (IsChecked(-selectedPiece.side))
				{
					Toast.makeText(getBaseContext(), (selectedPiece.side == 1 ? "Yellow" : "Red") + " checks!", Toast.LENGTH_LONG).show();

					if (IsCheckMate(-selectedPiece.side))
					{
						if (!bPlayingBack)
						{
							record.outcome = selectedPiece.side == 1 ? Result.YellowMates : Result.RedMates;
							endWithText((selectedPiece.side == 1 ? "Yellow" : "Red") + " mates! Game Over!");
						}
					}
				}

				selectedPiece = null;

				inTurn = -inTurn;
			}
		}
	};

	private final View.OnClickListener OnPiece = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			Piece piece = (Piece) view;

			if (inTurn == piece.side)
			{
				if (selectedPiece != null && selectedPiece.getId() == piece.getId())
				{
					Recover();
					selectedPiece = null;
				}
				else
				{
					select(piece);
				}
			}
			else
			{
				if (selectedPiece != null && isValild(piece.col, piece.row))
				{
					//	Confirmed capture here

					if (!bPlayingBack)
					{
						ArrayList<Point> thisStep = new ArrayList<>();
						thisStep.add(new Point(selectedPiece.col, selectedPiece.row));
						thisStep.add(new Point(piece.col, piece.row));
						record.steps.add(thisStep);
					}

					ChessActivity.this.piece = selectedPiece;
					location = new Point(ChessActivity.this.piece.col, ChessActivity.this.piece.row);
					hasMoved = selectedPiece.Moved;
					kind = selectedPiece.Kind;
					moves.clear();
					moves.addAll(selectedPiece.LegalMoves);

					targetPiece = piece;

					undo.setEnabled(true);

					if (piece.Kind == Kind.King)
					{
						if (!bPlayingBack)
						{
							record.outcome = piece.side == 1 ? Result.RedMates : Result.YellowMates;
							endWithText("Game Over, " + (piece.side == 1 ? "Red" : "Yellow") + " mates!");
						}
					}

					piece.captured = true;
					piece.setVisibility(View.INVISIBLE);

					Recover();
					selectedPiece.MoveTo(piece.col, piece.row);

					if (IsChecked(-selectedPiece.side))
					{
						Toast.makeText(getBaseContext(), (selectedPiece.side == 1 ? "Yellow" : "Red") + " checks!", Toast.LENGTH_LONG).show();

						if (IsCheckMate(-selectedPiece.side))
						{
							if (!bPlayingBack)
							{
								record.outcome = selectedPiece.side == 1 ? Result.YellowMates : Result.RedMates;
								endWithText((selectedPiece.side == 1 ? "Yellow" : "Red") + " mates! Game Over!");
							}
						}
					}

					selectedPiece = null;

					inTurn = -inTurn;
				}
			}
		}
	};

	private void setLabels()
	{
		labels.clear();

		for (int i = 0; i < 16; i++)
		{
			Label title = new Label(this, i, true);
			layout.addView(title, 60, gridSide);
			labels.add(title);
			views.add(title);
		}

		for (int i = 0; i < 16; i++)
		{
			Label title = new Label(this, i, false);
			layout.addView(title, gridSide, 60);
			labels.add(title);
			views.add(title);
		}

		ConstraintSet set = new ConstraintSet();
		set.clone(layout);

		for (int i = 0; i < 8; i++)
		{
			set.connect(labels.get(i).getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
			if (i == 7)
			{
				set.connect(labels.get(i).getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
				set.setMargin(labels.get(i).getId(), 3, 40);
			}
			else
			{
				set.connect(labels.get(i).getId(), ConstraintSet.TOP, labels.get(i + 1).getId(), ConstraintSet.BOTTOM);
			}
		}

		for (int i = 8; i < 16; i++)
		{
			set.connect(labels.get(i).getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
			if (i == 15)
			{
				set.connect(labels.get(i).getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
				set.setMargin(labels.get(i).getId(), ConstraintSet.LEFT, 40);
			}
			else
			{
				set.connect(labels.get(i).getId(), ConstraintSet.TOP, labels.get(i + 1).getId(), ConstraintSet.BOTTOM);
			}
		}

		for (int i = 16; i < 24; i++)
		{
			set.connect(labels.get(i).getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
			if (i == 16)
			{
				set.connect(labels.get(i).getId(), ConstraintSet.LEFT, labels.get(0).getId(), ConstraintSet.RIGHT);
			}
			else
			{
				set.connect(labels.get(i).getId(), ConstraintSet.LEFT, labels.get(i - 1).getId(), ConstraintSet.RIGHT);
			}
		}

		for (int i = 24; i < 32; i++)
		{
			set.connect(labels.get(i).getId(), ConstraintSet.TOP, labels.get(0).getId(), ConstraintSet.BOTTOM);
			if (i == 24)
			{
				set.connect(labels.get(i).getId(), ConstraintSet.LEFT, labels.get(0).getId(), ConstraintSet.RIGHT);
			}
			else
			{
				set.connect(labels.get(i).getId(), ConstraintSet.LEFT, labels.get(i - 1).getId(), ConstraintSet.RIGHT);
			}
		}

		set.applyTo(layout);
	}

	private void setCells()
	{
		grids.clear();

		for (int i = 0; i < 64; i++)
		{
			Cell grid = new Cell(this, i);
			grid.setOnClickListener(OnGrid);
			layout.addView(grid, gridSide, gridSide);
			grids.add(grid);
			views.add(grid);
		}

		ConstraintSet set = new ConstraintSet();
		set.clone(layout);

		for (int i = 0; i < 64; i++)
		{
			if (i % 8 == 0)
			{
				set.connect(grids.get(i).getId(), ConstraintSet.LEFT, labels.get(0).getId(), ConstraintSet.RIGHT);
			}
			else
			{
				set.connect(grids.get(i).getId(), ConstraintSet.LEFT, grids.get(i - 1).getId(), ConstraintSet.RIGHT);
			}

			if (i / 8 == 7)
			{
				set.connect(grids.get(i).getId(), ConstraintSet.TOP, labels.get(16).getId(), ConstraintSet.BOTTOM);
			}
			else
			{
				set.connect(grids.get(i).getId(), ConstraintSet.TOP, grids.get(i + 8).getId(), ConstraintSet.BOTTOM);
			}
		}

		set.applyTo(layout);
	}

	private void setPieces()
	{
		for (int i = 0; i < 8; i++)
		{
			Pawn pawn0 = new Pawn(this, 1, Kind.Pawn, this);
			pawn0.setOnClickListener(OnPiece);
			layout.addView(pawn0, gridSide, gridSide);
			blackPieces.add(pawn0);
			views.add(pawn0);

			Pawn pawn1 = new Pawn(this, -1, Kind.Pawn, this);
			pawn1.setOnClickListener(OnPiece);
			layout.addView(pawn1, gridSide, gridSide);
			whitePieces.add(pawn1);
			views.add(pawn1);
		}

		for (int i = 0; i < 2; i++)
		{
			Piece rook0 = new Piece(this, 1, Kind.Rook, this);
			rook0.setOnClickListener(OnPiece);
			layout.addView(rook0, gridSide, gridSide);
			blackPieces.add(rook0);
			views.add(rook0);

			Piece rook1 = new Piece(this, -1, Kind.Rook, this);
			rook1.setOnClickListener(OnPiece);
			layout.addView(rook1, gridSide, gridSide);
			whitePieces.add(rook1);
			views.add(rook1);

			Piece knight0 = new Piece(this, 1, Kind.Knight, this);
			knight0.setOnClickListener(OnPiece);
			layout.addView(knight0, gridSide, gridSide);
			blackPieces.add(knight0);
			views.add(knight0);

			Piece knight1 = new Piece(this, -1, Kind.Knight, this);
			knight1.setOnClickListener(OnPiece);
			layout.addView(knight1, gridSide, gridSide);
			whitePieces.add(knight1);
			views.add(knight1);

			Piece bishop0 = new Piece(this, 1, Kind.Bishop, this);
			bishop0.setOnClickListener(OnPiece);
			layout.addView(bishop0, gridSide, gridSide);
			blackPieces.add(bishop0);
			views.add(bishop0);

			Piece bishop1 = new Piece(this, -1, Kind.Bishop, this);
			bishop1.setOnClickListener(OnPiece);
			layout.addView(bishop1, gridSide, gridSide);
			whitePieces.add(bishop1);
			views.add(bishop1);
		}

		Piece queen0 = new Piece(this, 1, Kind.Queen, this);
		queen0.setOnClickListener(OnPiece);
		layout.addView(queen0, gridSide, gridSide);
		blackPieces.add(queen0);
		views.add(queen0);

		Piece queen1 = new Piece(this, -1, Kind.Queen, this);
		queen1.setOnClickListener(OnPiece);
		layout.addView(queen1, gridSide, gridSide);
		whitePieces.add(queen1);
		views.add(queen1);

		Piece king0 = new Piece(this, 1, Kind.King, this);
		king0.setOnClickListener(OnPiece);
		layout.addView(king0, gridSide, gridSide);
		blackPieces.add(king0);
		views.add(king0);

		Piece king1 = new Piece(this, -1, Kind.King, this);
		king1.setOnClickListener(OnPiece);
		layout.addView(king1, gridSide, gridSide);
		whitePieces.add(king1);
		views.add(king1);

		//
		for (int i = 0; i < 8; i++)
		{
			blackPieces.get(i).InitLocation(i, 1);
			whitePieces.get(i).InitLocation(i, 6);
		}

		for (int i = 8; i < 11; i++)
		{
			blackPieces.get(i).InitLocation(i % 8, 0);
			whitePieces.get(i).InitLocation(i % 8, 7);
		}

		for (int i = 11; i < 14; i++)
		{
			blackPieces.get(i).InitLocation(7 - i % 11, 0);
			whitePieces.get(i).InitLocation(7 - i % 11, 7);
		}

		blackPieces.get(14).InitLocation(3, 0);
		whitePieces.get(14).InitLocation(3, 7);

		blackPieces.get(15).InitLocation(4, 0);
		whitePieces.get(15).InitLocation(4, 7);
	}

	private boolean isValild(int col, int row)
	{
		for (int i = 0; i < validMoves.size(); i++)
		{
			if (col == validMoves.get(i).x && row == validMoves.get(i).y)
			{
				return true;
			}
		}

		return false;
	}

	private ArrayList<Piece> getSidePiece(int side)
	{
		ArrayList<Piece> SidePieces = new ArrayList<>();

		ArrayList<Piece> AllPieces = new ArrayList<>();
		AllPieces.addAll(blackPieces);
		AllPieces.addAll(whitePieces);

		for (int i = 0; i < AllPieces.size(); i++)
		{
			if (AllPieces.get(i).side == side && !AllPieces.get(i).captured)
			{
				SidePieces.add(AllPieces.get(i));
			}
		}

		return SidePieces;
	}

	private void select(Piece piece)
	{
		Recover();

		selectedPiece = piece;

		Tick(piece.col, piece.row, Colors.SELECTED_BKG);

		if (piece.Kind != Kind.Pawn)
		{
			validMoves = piece.GetRealLegalMoves();
		}
		else
		{
			Pawn pawn = (Pawn) piece;
			validMoves = pawn.GetRealLegalMoves();
		}

		Tick(validMoves);
	}

	private void Recover()
	{
		if (selectedPiece != null)
		{
			Recover(selectedPiece.col, selectedPiece.row);
		}

		for (int i = 0; i < validMoves.size(); i++)
		{
			Recover(validMoves.get(i).x, validMoves.get(i).y);
		}

		validMoves.clear();
	}

	private void Recover(int col, int row)
	{
		grids.get(row * 8 + col).reset();
	}

	private void Tick(ArrayList<Point> Points)
	{
		for (int i = 0; i < Points.size(); i++)
		{
			Tick(Points.get(i).x, Points.get(i).y, Colors.VALID);
		}
	}

	private void Tick(int col, int row, int fill)
	{
		grids.get(row * 8 + col).select(fill);
	}

	public ArrayList<Point> GetAllMoves(int side)
	{
		ArrayList<Piece> SidePieces = getSidePiece(side);
		ArrayList<Point> AllMoves = new ArrayList<>();

		for (int i = 0; i < SidePieces.size(); i++)
		{
			AllMoves.addAll(SidePieces.get(i).GetLegalMoves());
		}

		return AllMoves;
	}

	private boolean IsCheckMate(int side)
	{
		ArrayList<Piece> SidePieces = getSidePiece(side);

		for (int i = 0; i < SidePieces.size(); i++)
		{
			Piece candidate = SidePieces.get(i);
			int OriginX = candidate.col;
			int OriginY = candidate.row;

			ArrayList<Point> RealLegalMoves = candidate.GetRealLegalMoves();

			for (int j = 0; j < RealLegalMoves.size(); j++)
			{
				candidate.col = RealLegalMoves.get(j).x;
				candidate.row = RealLegalMoves.get(j).y;

				if (!candidate.IsChecked(candidate.side))
				{
					candidate.col = OriginX;
					candidate.row = OriginY;

					return false;
				}
			}

			candidate.col = OriginX;
			candidate.row = OriginY;
		}

		return true;
	}

	@Override
	public boolean IsChecked(int side)
	{
		ArrayList<Point> AllOpponentMoves = GetAllMoves(-side);

		Piece king = side == 1 ? blackPieces.get(15) : whitePieces.get(15);

		for (int i = 0; i < AllOpponentMoves.size(); i++)
		{
			if (AllOpponentMoves.get(i).x == king.col && AllOpponentMoves.get(i).y == king.row)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public Piece GetAliveOccupant(int col, int row)
	{
		for (int i = 0; i < 16; i++)
		{
			if (whitePieces.get(i).col == col && whitePieces.get(i).row == row && !whitePieces.get(i).captured)
			{
				return whitePieces.get(i);
			}

			if (blackPieces.get(i).col == col && blackPieces.get(i).row == row && !blackPieces.get(i).captured)
			{
				return blackPieces.get(i);
			}
		}

		return null;
	}

	@Override
	public int GetGridID(int col, int row)
	{
		return grids.get(row * 8 + col).getId();
	}

	@Override
	public ConstraintLayout GetLayout()
	{
		return findViewById(R.id.SuperLayout);
	}

	public void onResign(View view)
	{
		record.outcome = inTurn == 1 ? Result.YellowResign : Result.RedResign;
		endWithText("Resigned. Game Over!");
	}

	public void onDraw(View view)
	{
		record.outcome = inTurn == 1 ? Result.YellowDrawn : Result.RedDrawn;
		endWithText("Drawn. Game Over!");
	}

	public void onAI(View view)
	{
		ArrayList<Piece> SidePieces = getSidePiece(inTurn);
		ArrayList<Piece> Candidates = new ArrayList<>();

		for (int i = 0; i < SidePieces.size(); i++)
		{
			if (SidePieces.get(i).GetRealLegalMoves().size() != 0)
			{
				Candidates.add(SidePieces.get(i));
			}
		}

		if (Candidates.size() == 0)
		{
			Toast.makeText(getBaseContext(), "No piece can be moved.", Toast.LENGTH_LONG).show();
			return;
		}

		int seed = (int) (Math.random() * 10);

		Piece Candidate = Candidates.get(seed % Candidates.size());

		ArrayList<Point> RealLegalMoves = Candidate.GetRealLegalMoves();

		Point Destination = RealLegalMoves.get(seed % RealLegalMoves.size());

		Candidate.callOnClick();

		for (int i = 0; i < validMoves.size(); i++)
		{
			Recover(validMoves.get(i).x, validMoves.get(i).y);
		}

		Tick(Destination.x, Destination.y, Colors.TARGET);

		DelayClick(Destination.x, Destination.y);
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	public void onUndo(View view)
	{
		undo.setEnabled(false);

		piece.MoveTo(location.x, location.y);
		piece.Moved = hasMoved;
		piece.Kind = kind;

		if (kind == Kind.Pawn)
		{
			piece.setBackground(getResources().getDrawable(-inTurn > 0 ? R.drawable.pawn0 : R.drawable.pawn1, null));
		}

		piece.LegalMoves.clear();
		piece.LegalMoves.addAll(moves);

		if (targetPiece != null)
		{
			targetPiece.setVisibility(View.VISIBLE);
			targetPiece.captured = false;
		}

		if (record.steps.size() != 0)
		{
			record.steps.remove(record.steps.size() - 1);
		}

		inTurn = -inTurn;
	}

	public void onHidden(View view)
	{
		String message = "";

		switch (record.outcome)
		{
			case Init:
			{
				message = "Init.";
				break;
			}

			case YellowMates:
			{
				message = "Yellow mates. The playback ends.";
				break;
			}

			case YellowResign:
			{
				message = "Yellow resigns. The playback ends.";
				break;
			}

			case YellowDrawn:
			{
				message = "Yellow draws. The playback ends.";
				break;
			}

			case RedMates:
			{
				message = "Red mates. The playback ends.";
				break;
			}

			case RedResign:
			{
				message = "Red resigns. The playback ends.";
				break;
			}

			case RedDrawn:
			{
				message = "Red draws. The playback ends.";
				break;
			}
		}

		Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
		Reset();
	}

	private void DelayClick(int col, int row)
	{
		new DelayedClick(col, row).start();
	}

	private static class UIHandler extends Handler
	{
		private final WeakReference<ChessActivity> mActivity;

		private UIHandler(ChessActivity activity)
		{
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message message)
		{
			ChessActivity activity = mActivity.get();
			if (activity != null)
			{
				activity.findViewById(message.what).callOnClick();
			}
		}
	}

	private class DelayedClick extends Thread
	{
		private final int col;
		private final int row;

		private DelayedClick(int col, int row)
		{
			this.col = col;
			this.row = row;
		}

		public void run()
		{
			super.run();

			SystemClock.sleep(1000);

			Piece occupant = GetAliveOccupant(col, row);

			Message message = new Message();
			message.what = occupant != null ? occupant.getId() : grids.get(row * 8 + col).getId();

			mHandler.sendMessage(message);
		}
	}

	private void endWithText(String message)
	{
		Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();

		final EditText titlePrompt = new EditText(this);
		titlePrompt.setFocusable(true);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(getString(R.string.AskRecord));
		builder.setView(titlePrompt);
		builder.setNegativeButton(getString(R.string.Cancel),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						Toast.makeText(getBaseContext(), "Cancelled.", Toast.LENGTH_LONG).show();
						Reset();
					}
				});
		builder.setPositiveButton(getString(R.string.Save),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						if (titlePrompt.getText().toString().length() == 0)
						{
							record.title = "Null Name";
						}
						else
						{
							record.title = titlePrompt.getText().toString();
						}
						db.InsertRecord(record);
						Toast.makeText(getBaseContext(), "Saved.", Toast.LENGTH_LONG).show();
						Reset();
					}
				});
		builder.show();
	}

	private void Reset()
	{
		for (int i = 0; i < views.size(); i++)
		{
			layout.removeView(views.get(i));
		}
		views.clear();

		labels.clear();
		grids.clear();

		blackPieces.clear();
		whitePieces.clear();

		selectedPiece = null;
		validMoves.clear();

		inTurn = 1;

		resign = null;
		draw = null;
		ai = null;
		undo = null;

		record.steps.clear();

		moves.clear();

		bPlayingBack = false;

		setLabels();
		setCells();
		setPieces();
	}
}