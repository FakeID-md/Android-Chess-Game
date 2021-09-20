package com.example.chess;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

class Database
{
	private final SQLiteDatabase kDB;

	Database(Context context)
	{
		DBHelper dbHelper = new DBHelper(context, "Chess.db", 1);
		kDB = dbHelper.getWritableDatabase();
	}

	void InsertRecord(Record record)
	{
		kDB.execSQL("insert into Records(title, dateTime, steps, outcome) values(?, ? ,? ,? )", new String[]{record.title, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(new Date(System.currentTimeMillis())), record.GetSteps(), record.GetOutcome()});
	}

	ArrayList<Record> Read()
	{
		ArrayList<Record> Records = new ArrayList<>();

		Cursor cursor =  kDB.rawQuery("select * from Records", null);

		while (cursor.moveToNext())
		{
			int ID = cursor.getInt(cursor.getColumnIndex("rid"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String time = cursor.getString(cursor.getColumnIndex("dateTime"));
			String steps = cursor.getString(cursor.getColumnIndex("steps"));
			int outcome = cursor.getInt(cursor.getColumnIndex("outcome"));

			Record tuple = new Record();
			tuple.ID = ID;
			tuple.title = title;
			tuple.time = time;
			tuple.SetSteps(steps);
			tuple.SetOutcome(outcome);

			Records.add(tuple);
		}

		cursor.close();

		return Records;
	}
}