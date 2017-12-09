package com.oguzparlak.wakemeup.ui.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.http.MatrixDistanceApiClient;
import com.oguzparlak.wakemeup.model.MatrixDistanceModel;
import com.oguzparlak.wakemeup.service.GeofenceTransitionIntentService;
import com.oguzparlak.wakemeup.ui.adapter.SectionsPagerAdapter;
import com.oguzparlak.wakemeup.ui.callbacks.DistanceMatrixCallback;
import com.oguzparlak.wakemeup.ui.callbacks.GooglePlaceSelectionListener;
import com.oguzparlak.wakemeup.ui.callbacks.ListItemClickListener;
import com.oguzparlak.wakemeup.ui.callbacks.TaskListFragmentCallbacks;
import com.oguzparlak.wakemeup.ui.fragment.MapFragment;
import com.oguzparlak.wakemeup.ui.fragment.TaskListFragment;
import com.oguzparlak.wakemeup.utils.GeofenceBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        ListItemClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        DistanceMatrixCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int AUTO_COMPLETE_REQUEST = 5;

    /**
     * Views
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.bottom_sheet)
    View mBottomSheet;

    private GoogleApiClient mGoogleApiClient;

    private TaskListFragmentCallbacks mTaskListFragmentCallbacks;

    // Callback of AutoCompleteWidget
    private GooglePlaceSelectionListener mPlaceSelectionListener;

    // BottomSheet View Components
    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView mAddressInfoTextView;
    private TextView mAddressIndicator;
    private TextView mDrivingInfoTextView;
    private TextView mWalkingInfoTextView;
    private TextView mTransitInfoTextView;
    private ImageView mErrorImageView;
    private TextView mErrorTextView;
    private Button mAddGeofenceButton;

    /**
     * Geofencing client will register or
     * unregiser Geofences
     */
    private GeofencingClient mGeofencingClient;

    /**
     * Will be used to calculate GeofenceService
     */
    private PendingIntent mGeofencingPendingIntent;

    /**
     * Utility class to build geofences
     */
    private GeofenceBuilder mGeofenceBuilder;

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

        // Get GeofencingClient
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mGeofenceBuilder = new GeofenceBuilder();

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_map_white_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_location_on_white_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_transfer_within_a_station_white_24dp);

        // Hide BottomSheet by default
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomSheetBehavior.setHideable(true);

        // State Listener
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // do nothing for now
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Change the alpha of the fab
                // as the BottomSheet slides
                if (slideOffset <= 0 && slideOffset >= -1)
                     mAddGeofenceButton.setAlpha(slideOffset + 1);
                // If the slideOffset has reached the threshold
                // Disable it, otherwise enable.
                if (slideOffset <= -1) {
                    mAddGeofenceButton.setEnabled(false);
                } else {
                    mAddGeofenceButton.setEnabled(true);
                }
            }
        });

        mAddressInfoTextView = mBottomSheet.findViewById(R.id.address_info_text_view);
        mAddressIndicator = mBottomSheet.findViewById(R.id.destination_label);
        mDrivingInfoTextView = mBottomSheet.findViewById(R.id.driving_info_text_view);
        mWalkingInfoTextView = mBottomSheet.findViewById(R.id.walking_info_text_view);
        mTransitInfoTextView = mBottomSheet.findViewById(R.id.transit_info_text_view);
        mErrorImageView = mBottomSheet.findViewById(R.id.error_image_view);
        mErrorTextView = mBottomSheet.findViewById(R.id.error_text_view);
        mAddGeofenceButton = mBottomSheet.findViewById(R.id.add_geofence_button);
    }

    private PendingIntent getGeofencingPendingIntent() {
        // Reuse the PendingIntent if it already exists
        if (mGeofencingPendingIntent != null) {
            return mGeofencingPendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencingPendingIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencingPendingIntent;
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

    @SuppressLint("MissingPermission")
    @OnClick(R.id.add_geofence_button)
    void addGeofenceButtonClicked() {
        // TODO Add the Geofence, Need Callback
        mGeofenceBuilder.addGeofence(null);
        // Create a Geofencing Request
        mGeofencingClient.addGeofences(mGeofenceBuilder.getGeofencingRequest(),
                getGeofencingPendingIntent())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "addGeofenceButtonClicked: geofences_added: should show someting on UI Thread");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "addGeofenceButtonClicked: geofence_add_failed: should show error");
                });
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

    /**
     * DistanceMatrixCallbacks
     */
    @Override
    public void onPrepare() {
        // Show a progress bar to user
        ProgressBar progressBar = mBottomSheet.findViewById(R.id.bottom_sheet_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // Hide other View components
        mAddressInfoTextView.setVisibility(View.INVISIBLE);
        mAddressIndicator.setVisibility(View.INVISIBLE);

        // Pop BottomSheet
        mBottomSheetBehavior.setPeekHeight(800);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    /**
     * Triggered when a model is received from fragment
     */
    @Override
    public void onModelReceived(MatrixDistanceModel model) {
        // Bottom Sheet
        runOnUiThread(() -> {

            // Hide the progressbar
            ProgressBar progressBar = mBottomSheet.findViewById(R.id.bottom_sheet_progress_bar);
            progressBar.setVisibility(View.INVISIBLE);

            if (model == null) {
                toggleBottomSheetComponents(true);
                return;
            } else {
                toggleBottomSheetComponents(false);
            }

            mAddressInfoTextView.setText(model.getDestinationAddress());
            mAddressIndicator.setText(model.getDestinationAddress().substring(0, 1));

            String travelInformation = model.getDistance() + " - " + model.getDuration();

            switch (model.getTravelType()) {
                case MatrixDistanceApiClient.MODE_DRIVING:
                    mDrivingInfoTextView.setText(travelInformation);
                    break;
                case MatrixDistanceApiClient.MODE_WALKING:
                    mWalkingInfoTextView.setText(travelInformation);
                    break;
                case MatrixDistanceApiClient.MODE_TRANSIT:
                    mTransitInfoTextView.setText(travelInformation);
                    break;
            }

            // If desired location is more than 5 km away
            // Don't update the walking info
            String distance = model.getDistance().split(" ")[0];
            if (distance.compareTo("5") > 0) {
                mWalkingInfoTextView.setText("-");
            }

        });
    }

    private void toggleBottomSheetComponents(boolean shouldShowError) {
        final int visibility = shouldShowError ? View.INVISIBLE : View.VISIBLE;
        mAddressInfoTextView.setVisibility(visibility);
        mAddressIndicator.setVisibility(visibility);
        mAddGeofenceButton.setVisibility(visibility);
        if (shouldShowError) {
            mErrorImageView.setVisibility(View.VISIBLE);
            mErrorTextView.setVisibility(View.VISIBLE);
            mWalkingInfoTextView.setText(" - ");
            mDrivingInfoTextView.setText(" - ");
            mTransitInfoTextView.setText(" - ");
        } else {
            mErrorTextView.setVisibility(View.INVISIBLE);
            mErrorTextView.setVisibility(View.INVISIBLE);
        }

    }

}
