package com.example.chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
	Database kDB;

	private ListView kList;

	ArrayList<Record> kRecords = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		kDB = new Database(getBaseContext());

		kList = findViewById(R.id.list);

		resetTable(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		resetTable(true);
	}

	AdapterView.OnItemClickListener OnItem = (parent, view, position, id) ->
	{
		Intent intent = new Intent(MainActivity.this, ChessActivity.class);
		intent.putExtra("IsPlayback", true);
		intent.putExtra("Steps", kRecords.get(position).GetSteps());
		intent.putExtra("Outcome", kRecords.get(position).outcome.ordinal());
		startActivity(intent);
	};

	private void resetTable(boolean sortByTime)
	{
		kList.setAdapter(null);

		kRecords.clear();
		kRecords = kDB.Read();

		if (sortByTime)
		{
			kRecords.sort(new DatetimeSorter());
		}
		else
		{
			kRecords.sort(new TitleSorter());
		}

		ArrayList<String> Items = new ArrayList<>();

		for (int i = 0; i < kRecords.size(); i++)
		{
			Items.add(kRecords.get(i).time + " - " + kRecords.get(i).title);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, Items);
		kList.setAdapter(adapter);
		kList.setOnItemClickListener(OnItem);
	}

	public void onNewGame(View view)
	{
		Intent intent = new Intent(MainActivity.this, ChessActivity.class);
		intent.putExtra("IsPlayback", false);
		startActivity(intent);
	}

	public void onTime(View view)
	{
		resetTable(true);
	}

	public void onTitle(View view)
	{
		resetTable(false);
	}
}