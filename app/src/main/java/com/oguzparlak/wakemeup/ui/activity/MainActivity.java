package com.oguzparlak.wakemeup.ui.activity;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.constants.Constants;
import com.oguzparlak.wakemeup.provider.TaskContract;
import com.oguzparlak.wakemeup.ui.adapter.SectionsPagerAdapter;
import com.oguzparlak.wakemeup.ui.callbacks.GooglePlaceSelectionListener;
import com.oguzparlak.wakemeup.ui.callbacks.ListItemClickListener;
import com.oguzparlak.wakemeup.ui.fragment.MapFragment;
import com.oguzparlak.wakemeup.ui.fragment.TaskListFragment;
import com.oguzparlak.wakemeup.ui.callbacks.TaskListFragmentCallbacks;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        ListItemClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PLACE_PICKER_REQUEST = 2;
    private static final int AUTO_COMPLETE_REQUEST = 5;

    /**
     * Views
     */
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.add_location_fab) FloatingActionButton mFab;
    @BindView(R.id.view_pager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;

    private GoogleApiClient mGoogleApiClient;

    private TaskListFragmentCallbacks mTaskListFragmentCallbacks;

    // Callback of AutoCompleteWidget
    private GooglePlaceSelectionListener mPlaceSelectionListener;

    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply ButterKnife
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        setupGoogleApiClient();

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_map_white_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_location_on_white_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_transfer_within_a_station_white_24dp);

    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof TaskListFragment) {
            mTaskListFragmentCallbacks = (TaskListFragmentCallbacks) fragment;
        } else if (fragment instanceof MapFragment) {
            mPlaceSelectionListener = (GooglePlaceSelectionListener) fragment;
        }
    }

    /**
     * Add Google APIs here
     */
    private synchronized void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .build();
    }

    @OnClick (R.id.add_location_fab)
    void addFabClicked() {
        // TEST
        // Pop BottomSheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        // END TEST
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTO_COMPLETE_REQUEST) {
            if (resultCode == RESULT_OK) {
                mPlaceSelectionListener.onPlaceSelected(PlaceAutocomplete.getPlace(this, data));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // Handle error
                Log.e(TAG, "onActivityResult: AutoCompleteWidget: "
                        + PlaceAutocomplete.getStatus(this, data).getStatusMessage());
            }
        }
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
            case R.id.action_search:
                // Open AutoCompleteWidget
                openAutoCompleteIntent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens the AutoComplete Intent
     * The result of this operation must be handled in
     * onActivityResult method
     */
    private void openAutoCompleteIntent() {
        try {
            // Initialize the Intent
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, AUTO_COMPLETE_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
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

    /**
     * TODO Implement it later...
     * RecyclerView Callbacks.
     */
    @Override
    public void onItemClicked(View v, int position, int id) { }

    @Override
    public void onCheckChanged(boolean checked, int id) { }
}
