package com.example.audiofy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pdf_converter.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "pdf_history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "pdf_name";
    public static final String COLUMN_URI = "pdf_uri";
    public static final String COLUMN_DATE = "opened_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_URI + " TEXT, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addHistory(String name, String uri, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_URI, uri);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<HashMap<String, String>> getAllHistory() {
        ArrayList<HashMap<String, String>> historyList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> item = new HashMap<>();
                item.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                item.put("date", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                historyList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }
}