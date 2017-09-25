package com.oguzparlak.wakemeup.ui;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.provider.TaskContract;
import com.oguzparlak.wakemeup.ui.adapter.TaskRecyclerAdapter;
import com.oguzparlak.wakemeup.utils.ColorUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ListItemClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int LOADER_ID = 1;
    private static final int PLACE_PICKER_REQUEST = 2;
    private static final int LOCATION_REQUEST = 3;

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
    @BindView(R.id.default_message_text_view) TextView mDefaultTextView;

    private GoogleApiClient mGoogleApiClient;
    private TaskRecyclerAdapter mTaskAdapter;

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

        // Start the loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        setupGoogleApiClient();
    }

    private synchronized void setupGoogleApiClient() {
        // Setup Google Api Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .build();
    }

    // Requests Location Permission
    // Open Place Picker Intent
    @OnClick (R.id.fab)
    void addFabClicked() {
        if (PermissionChecker.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            // Request Permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        } else {
            openPlacePicker();
        }
    }

    // Open the PlacePicker
    private void openPlacePicker() {
        try {
            Intent placePickerIntent = new PlacePicker.IntentBuilder()
                    .build(this);
            startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);
            hideUI();
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "addFabClicked: Exception: GooglePlayServices not available");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 &&
                        grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                    openPlacePicker();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Log.d(TAG, "onActivityResult: Selected Place Name: " + place.getName());
                showUI();
                // Test, Insert
                ContentValues contentValues = new ContentValues();
                contentValues.put(TaskContract.TaskEntry.COLUMN_PLACE_ID, place.getId());
                contentValues.put(TaskContract.TaskEntry.COLUMN_TAG, place.getName().toString());
                contentValues.put(TaskContract.TaskEntry.COLUMN_ACTIVE, 1);
                contentValues.put(TaskContract.TaskEntry.COLUMN_COLOR, ColorUtils.getRandomColor(this));
                contentValues.put(TaskContract.TaskEntry.COLUMN_RADIUS, 1);
                // END TEST
                getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);
                // Open TaskPreferencesActivity
                // Intent preferenceIntent = new Intent(MainActivity.this, TaskPreferencesActivity.class);
                // preferenceIntent.putExtra(TaskPreferencesActivity.PLACE_NAME_EXTRA, place.getName());
                // tartActivity(preferenceIntent);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: User cancelled the Place Picker Intent");
                showUI();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideUI() {
        mLocationRecycler.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mDefaultTextView.setVisibility(View.INVISIBLE);
    }

    private void showUI() {
        mLocationRecycler.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mDefaultTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    /**
     * Menu
     */
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
                TaskContract.TaskEntry.COLUMN_ACTIVE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        showUI();

        // Set the adapter
        if (mTaskAdapter == null) {
            mTaskAdapter = new TaskRecyclerAdapter(cursor, this, this);
            mLocationRecycler.setAdapter(mTaskAdapter);
        }

        // Show or hide the default text message
        if (mTaskAdapter.getItemCount() > 0)
            mDefaultTextView.setVisibility(View.INVISIBLE);
        else
            mDefaultTextView.setVisibility(View.VISIBLE);

        mTaskAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTaskAdapter.swapCursor(null);
    }

    /**
     * RecyclerView Touch Events Callbacks
     */
    @Override
    public void onItemClicked(View v, int position, int id) {
        Intent intent = new Intent(MainActivity.this, TaskPreferencesActivity.class);
        Cursor cursor = getContentResolver()
                .query(ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id),
                PROJECTION,
                "_id=?",
                new String[]{String.valueOf(id)},
                null);
        if (cursor != null) {
            cursor.moveToPosition(0);
            String placeName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TAG));
            intent.putExtra(TaskPreferencesActivity.PLACE_NAME_EXTRA, placeName);
            startActivity(intent);
            cursor.close();
        }
    }

    // Update the database according to value of the switch
    @Override
    public void onCheckChanged(boolean checked, final int id) {
        final int checkedValue = checked ? 1 : 0;
        new AsyncTask<Void, Void, Void>() {
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

    /**
     * Google Api Callbacks
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }
}
