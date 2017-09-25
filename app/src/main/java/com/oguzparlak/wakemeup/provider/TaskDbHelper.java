package com.oguzparlak.wakemeup.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.oguzparlak.wakemeup.provider.TaskContract.TaskEntry.TABLE_NAME;

public class TaskDbHelper extends SQLiteOpenHelper {

    // The database name
    public static final String DB_NAME = "tasks.db";

    // If you change the database schema, you must increment the database version.
    private static final int DB_VERSION = 1;

    public TaskDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_ACTIVE + " INTEGER, " +
                TaskContract.TaskEntry.COLUMN_RADIUS + " INTEGER, " +
                TaskContract.TaskEntry.COLUMN_TAG + " TEXT, " +
                TaskContract.TaskEntry.COLUMN_COLOR + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
