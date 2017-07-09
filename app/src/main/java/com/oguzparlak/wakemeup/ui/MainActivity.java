package com.oguzparlak.wakemeup.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.provider.TaskContract;
import com.oguzparlak.wakemeup.utils.ColorUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        RecyclerViewOnClickListener, RecyclerViewOnCheckedChangedListener {

    // TODO Provide a new interface to add a new Location by using Google Places API

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOADER_ID = 1;

    // Projection, define which columns to return
    private static final String[] PROJECTION =
                          {
                                  TaskContract.TaskEntry._ID ,
                                  TaskContract.TaskEntry.COLUMN_PLACE_ID,
                                  TaskContract.TaskEntry.COLUMN_ACTIVE,
                                  TaskContract.TaskEntry.COLUMN_TAG,
                                  TaskContract.TaskEntry.COLUMN_COLOR,
                                  TaskContract.TaskEntry.COLUMN_RADIUS
                          };

    /**
     * Views
     */
    @BindView(R.id.saved_places_recycler) RecyclerView mLocationRecycler;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    private TaskAdapter mTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply ButterKnife
        ButterKnife.bind(this);

        mToolbar.setTitle(R.string.locations);
        setSupportActionBar(mToolbar);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLocationRecycler.setHasFixedSize(true);
        mLocationRecycler.setLayoutManager(layoutManager);

        // Dividers
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mLocationRecycler.getContext(),
                layoutManager.getOrientation()
        );
        mLocationRecycler.addItemDecoration(dividerItemDecoration);
        mTaskAdapter = new TaskAdapter(this, this, this);
        mLocationRecycler.setAdapter(mTaskAdapter);

        // Hide fab according to RecyclerView's scroll behavior
        mLocationRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && mFab.isShown()) {
                    mFab.hide();
                }
            }
        });

        /*

        if (savedInstanceState == null) {
            // Insert some dummy data
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COLUMN_TAG, "Santa Clara Valley");
            values.put(TaskContract.TaskEntry.COLUMN_PLACE_ID, "123");
            values.put(TaskContract.TaskEntry.COLUMN_RADIUS, 1);
            values.put(TaskContract.TaskEntry.COLUMN_ACTIVE, 1);
            values.put(TaskContract.TaskEntry.COLUMN_COLOR, ColorUtils.getRandomColor(this));

            getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values);
        }

        */

        // Start the loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @OnClick (R.id.fab)
    void addFabClicked(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_settings:
                Log.d(TAG, "onOptionsItemSelected: Settings Clicked");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loader Callbacks
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                TaskContract.TaskEntry.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: ");
        mProgressBar.setVisibility(View.INVISIBLE);
        mTaskAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: ");
        mTaskAdapter.swapCursor(null);
    }

    /**
     * RecyclerView Touch Events Callbacks
     */
    // Create a new Intent to PreferenceActivity
    @Override
    public void onItemClicked(View v, int position, int id) {
        Log.d(TAG, "onItemClicked: " + String.format("The position is: %d, the id is: %d", position, id));
    }

    // Update the database according to value of the switch
    @Override
    public void onCheckChanged(boolean checked, final int id) {
        String status = checked ? "Checked" : "Unchecked";
        final int checkedValue = checked ? 1 : 0;
        Log.d(TAG, "onCheckChanged: The item with id: " + id + " is now: " + status);
        AsyncTask<Void, Void, Void> updateTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TaskContract.TaskEntry.COLUMN_ACTIVE, checkedValue);
                getContentResolver().update(
                        ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id),
                        contentValues,
                        "_id=?",
                        new String[]{String.valueOf(id)});
                return null;
            }
        }.execute();
    }
}
