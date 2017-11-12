package com.oguzparlak.wakemeup.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.oguzparlak.wakemeup.R;
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

import java.util.List;

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
    @BindView(R.id.add_location_fab)
    FloatingActionButton mFab;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.bottom_sheet)
    View mBottomSheet;

    // Fab
    CoordinatorLayout.LayoutParams mFabLayoutParams;

    private GoogleApiClient mGoogleApiClient;

    private TaskListFragmentCallbacks mTaskListFragmentCallbacks;

    // Callback of AutoCompleteWidget
    private GooglePlaceSelectionListener mPlaceSelectionListener;

    // BottomSheet View Components
    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView mDestinationAddressTextView;
    private TextView mDurationTextView;
    private TextView mDistanceTextView;
    private ImageView mCurrentLocationImageView;
    private View mDivider;
    private TextView mAddressIndicator;
    private TextView mCurrentLocationLabel;

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
                    mFab.setAlpha(slideOffset + 1);
                // If the slideOffset has reached the threshold
                // Disable it, otherwise enable.
                if (slideOffset <= -1) {
                    mFab.setClickable(false);
                } else {
                    mFab.setClickable(true);
                }
            }
        });

        mDestinationAddressTextView = mBottomSheet.findViewById(R.id.destination_address_text_view);
        mDurationTextView = mBottomSheet.findViewById(R.id.estimated_time_text_view);
        mDistanceTextView = mBottomSheet.findViewById(R.id.distance_text_view);
        mCurrentLocationImageView = mBottomSheet.findViewById(R.id.current_location_image_view);
        mDivider = mBottomSheet.findViewById(R.id.divider);
        mAddressIndicator = mBottomSheet.findViewById(R.id.destination_label);
        mCurrentLocationLabel = mBottomSheet.findViewById(R.id.current_location_label);

        mFabLayoutParams = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
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
    @OnClick(R.id.add_location_fab)
    void addFabClicked() {
        // TODO Add the Geofence, Need Callback
        mGeofenceBuilder.addGeofence(null);
        // Create a Geofencing Request
        mGeofencingClient.addGeofences(mGeofenceBuilder.getGeofencingRequest(),
                getGeofencingPendingIntent())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "addFabClicked: geofences_added: should show someting on UI Thread");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "addFabClicked: geofence_add_failed: should show error");
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

    @Override
    public void onPrepare() {
        // Show a progress bar to user
        ProgressBar progressBar = mBottomSheet.findViewById(R.id.bottom_sheet_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // Hide other View components
        mDestinationAddressTextView.setVisibility(View.INVISIBLE);
        mDurationTextView.setVisibility(View.INVISIBLE);
        mDistanceTextView.setVisibility(View.INVISIBLE);
        mCurrentLocationImageView.setVisibility(View.INVISIBLE);
        mDivider.setVisibility(View.INVISIBLE);
        mAddressIndicator.setVisibility(View.INVISIBLE);
        mCurrentLocationLabel.setVisibility(View.INVISIBLE);

        // Pop BottomSheet
        mBottomSheetBehavior.setPeekHeight(600);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mFabLayoutParams.setAnchorId(R.id.bottom_sheet);
        mFab.setLayoutParams(mFabLayoutParams);

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

            // Set visible other components
            mDestinationAddressTextView.setVisibility(View.VISIBLE);
            mDurationTextView.setVisibility(View.VISIBLE);
            mDistanceTextView.setVisibility(View.VISIBLE);
            mCurrentLocationImageView.setVisibility(View.VISIBLE);
            mDivider.setVisibility(View.VISIBLE);
            mAddressIndicator.setVisibility(View.VISIBLE);
            mCurrentLocationLabel.setVisibility(View.VISIBLE);

            mDestinationAddressTextView.setText(model.getDestinationAddress());
            mDurationTextView.setText("Duration: " + model.getDuration());
            mDistanceTextView.setText("Distance: " + model.getDistance());
            mAddressIndicator.setText(model.getDestinationAddress().substring(0, 1));

            // Show fab
            mFab.show();


        });
    }
}
