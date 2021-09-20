package com.example.chess;

import android.graphics.Point;

import java.util.ArrayList;

class Record
{
	int ID;
	String title;
	String time;
	ArrayList<ArrayList<Point>> steps = new ArrayList<>();
	Result outcome;

	String GetSteps()
	{
		StringBuilder stepsString = new StringBuilder();

		for (int i = 0; i < steps.size(); i++)
		{
			stepsString.append(steps.get(i).get(0).x);
			stepsString.append(".");
			stepsString.append(steps.get(i).get(0).y);
			stepsString.append(".");
			stepsString.append(steps.get(i).get(1).x);
			stepsString.append(".");
			stepsString.append(steps.get(i).get(1).y);

			if (i != steps.size() - 1)
			{
				stepsString.append("$");
			}
		}

		return stepsString.toString();
	}

	void SetSteps(String stepsString)
	{
		steps.clear();

		String[] SingleSteps = stepsString.split("\\$");

		for (String singleStep : SingleSteps)
		{
			String[] Coordinates = singleStep.split("\\.");
			ArrayList<Point> SingleStep = new ArrayList<>();

			SingleStep.add(new Point(Integer.parseInt(Coordinates[0]), Integer.parseInt(Coordinates[1])));
			SingleStep.add(new Point(Integer.parseInt(Coordinates[2]), Integer.parseInt(Coordinates[3])));

			steps.add(SingleStep);
		}
	}

	String GetOutcome()
	{
		return String.valueOf(outcome.ordinal());
	}

	void SetOutcome(int value)
	{
		outcome = Result.values()[value];
	}
}