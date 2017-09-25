package com.oguzparlak.wakemeup.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class TaskProvider extends ContentProvider {

    private static final String TAG = TaskProvider.class.getSimpleName();

    // Define code for directories and the items
    // in that directory to be used in matching URIs.
    // It's conventional to use 100, 200, 300 or directories
    // and 101, 102 for items in that directories
    public static final int TASKS = 100;
    public static final int TASK_WITH_ID = 101;

    // Utility class to aid in matching URIs in ContentProviders
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS, TASKS);
        matcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS + "/#", TASK_WITH_ID);
        return matcher;
    }

    // Instance of TaskDbHelper
    private TaskDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new TaskDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        int matchCode = sUriMatcher.match(uri);
        Cursor cursor;
        // Match the uri
        switch (matchCode) {
            case TASKS:
                cursor = database.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_WITH_ID:
                // Get the id column
                String id = uri.getPathSegments().get(1);
                // Apply the selection filtering by id
                cursor = database.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unexpected Uri matching error occurred");
        }

        // Set a notification URI on the Cursor and return that cursor
        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int matchCode = sUriMatcher.match(uri);
        Uri returnUri;
        switch (matchCode) {
            case TASKS:
                long id = database.insert(TaskContract.TaskEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unexpected Uri matching error occurred");
        }

        // Notify the resolver if the uri has been changed
        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int matchCode = sUriMatcher.match(uri);
        int numRowsAffected = 0;
        switch (matchCode) {
            case TASK_WITH_ID:
                // Get the task id from the URI Path 0 -> Count, 1-> Id
                String id = uri.getPathSegments().get(1);
                // database.delete returns the # of rows affected
                numRowsAffected = database.delete(TaskContract.TaskEntry.TABLE_NAME, "_id=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unexpected Uri matching error occurred");
        }

        // Notify the resolver
        if (numRowsAffected > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsAffected;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int matchCode = sUriMatcher.match(uri);
        int numRowsAffected;
        switch (matchCode) {
            case TASK_WITH_ID:
                // Get the task id from the URI Path 0 -> Count, 1-> Id
                String id = uri.getPathSegments().get(1);
                numRowsAffected = database.update(TaskContract.TaskEntry.TABLE_NAME,
                        contentValues, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unexpected Uri matching error occurred");
        }

        // Notify if rows affected
        if (getContext() != null && numRowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsAffected;
    }
}
