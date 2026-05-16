package com.example.audiofy;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvHistory;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lvHistory = findViewById(R.id.lvHistory);
        dbHelper = new DatabaseHelper(this);

        displayHistory();
    }

    private void displayHistory() {
        ArrayList<HashMap<String, String>> historyList = dbHelper.getAllHistory();

        String[] from = {"name", "date"};
        int[] to = {R.id.tvItemPdfName, R.id.tvItemDate};

        SimpleAdapter adapter = new SimpleAdapter(this, historyList, R.layout.item_history, from, to);
        lvHistory.setAdapter(adapter);
    }
}