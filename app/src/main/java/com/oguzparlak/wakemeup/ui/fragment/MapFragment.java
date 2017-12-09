package com.oguzparlak.wakemeup.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.constants.Constants;
import com.oguzparlak.wakemeup.http.MatrixDistanceApiClient;
import com.oguzparlak.wakemeup.model.MatrixDistanceModel;
import com.oguzparlak.wakemeup.ui.adapter.MapAdapter;
import com.oguzparlak.wakemeup.ui.callbacks.DistanceMatrixCallback;
import com.oguzparlak.wakemeup.ui.callbacks.GooglePlaceSelectionListener;
import com.oguzparlak.wakemeup.ui.callbacks.HttpCallback;
import com.oguzparlak.wakemeup.utils.BitmapUtils;
import com.oguzparlak.wakemeup.utils.LocationUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Oguz Parlak
 * <p>
 * MapFragment contains a GoogleMapView and GoogleMap object
 * where user can ineract with map and add Tasks
 * </p/
 **/
public class MapFragment extends Fragment implements GooglePlaceSelectionListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int LOCATION_REQUEST = 201;

    /**
     * Unbinder for ButterKnife
     */
    private Unbinder mUnbinder;

    /**
     * GoogleMap instance which behaves
     * like the model of the map
     */
    private GoogleMap mGoogleMap;

    /**
     * LocationUtil instance will behave
     * like an adapter of Location and GoogleMap
     */
    private LocationUtil mLocationUtil;

    /**
     * Indicates the last known location
     */
    private Location mLastKnownLocation;

    /**
     * DistanceMatrixCallback to be passed into host Activity
     */
    private DistanceMatrixCallback mDistanceMatrixCallback;

    @BindView(R.id.map_view)
    MapView mGoogleMapView;
    private MapAdapter mMapAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDistanceMatrixCallback = (DistanceMatrixCallback) context;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        mGoogleMapView.onCreate(savedInstanceState);
        mGoogleMapView.onResume();

        MapsInitializer.initialize(getContext());

        mGoogleMapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Customize the Map
        try {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),
                    R.raw.google_maps_silver));
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "onMapReady: " + ex.getMessage());
        }

        // Prepare Map for current Location
        if (PermissionChecker.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: Yes");
            mGoogleMap.setMyLocationEnabled(true);
            prepareMapForCurrentLocation();
        } else {
            // Request Permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
        }

        // Init MapAdapter
        mMapAdapter = new MapAdapter(getContext(), mGoogleMap);

        // Hide myLocation Button
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Hide Route button and maps button
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        // MapClickListener
        mGoogleMap.setOnMapClickListener(this);

    }

    /**
     * Prepares the map for the current location
     */
    @SuppressLint("MissingPermission")
    private void prepareMapForCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(getContext())
                .getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();

                    // Init Location Util
                    mLocationUtil = new LocationUtil(mLastKnownLocation);

                    // Locate the camera to current location
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocationUtil.getLatLng(), 15));
                } else {
                    Log.e(TAG, "onComplete: Task failed due to some reason");
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            if (permissions.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(true);
                prepareMapForCurrentLocation();
            }
        }
    }

    /**
     * Place Selection Callback
     * from AutoCompleteWidget
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.d(TAG, "onPlaceSelected: place: " + place.getName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Location destination = new Location(LocationManager.GPS_PROVIDER);
        destination.setLatitude(latLng.latitude);
        destination.setLongitude(latLng.longitude);

        // The distance between current location and destination
        final float distance = mLocationUtil.getDistanceTo(destination);

        MatrixDistanceApiClient matrixDistanceApiClient = MatrixDistanceApiClient.getInstance();
        matrixDistanceApiClient.setSource(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        matrixDistanceApiClient.setDestination(latLng);

        String[] travelModes = buildTravelModes(distance).toArray(new String[0]);

        // Mark the desired point
        mMapAdapter.markPoint(latLng);

        // If the distance betweeen two location exceeds
        // the threshold then pass null object and display error
        if (distance > Constants.DISTANCE_THRESHOLD) {
            mDistanceMatrixCallback.onModelReceived(null);
            return;
        }

        if (isConnected()) {
            // Prepare the BottomSheet
            mDistanceMatrixCallback.onPrepare();

            // Make Api Call for three type of travel
            // Driving, Walking and Transit
            matrixDistanceApiClient.makeNestedCall(new HttpCallback() {
                @Override
                public void onFinished(String travelMode, int statusCode, JsonObject response) {
                    if (statusCode == 200) {
                        String destinationAddress = response
                                .getAsJsonArray("destination_addresses")
                                .get(0).getAsString();

                        JsonObject elementRoot =  response.getAsJsonArray("rows")
                                .get(0).getAsJsonObject().getAsJsonArray("elements")
                                .get(0).getAsJsonObject();

                        String distanceText = elementRoot.getAsJsonObject("distance")
                                .get("text").getAsString();

                        String duration = elementRoot.getAsJsonObject("duration")
                                .get("text").getAsString();

                        MatrixDistanceModel matrixDistanceModel =
                                new MatrixDistanceModel(travelMode, destinationAddress, distanceText, duration);

                        // Pass the model to Activity
                        mDistanceMatrixCallback.onModelReceived(matrixDistanceModel);

                    } else {
                        Log.e(TAG, "onMapClick: Http call ended with a failure: " + statusCode);
                    }

                }

                @Override
                public void onError(int errorCode) {
                    Log.e(TAG, "onError: errorCode: " + errorCode);
                    // pass the null object
                    mDistanceMatrixCallback.onModelReceived(null);
                }

            }, travelModes);
        } else {
            // Pop a dialog to user
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.no_internet_dialog, null))
                    // Add action buttons
                    .setPositiveButton(android.R.string.cancel, (dialog, id) -> {
                        // dismiss the dialog
                    })
                    .setNegativeButton(R.string.settings, (dialog, id) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    })
                    .setTitle(R.string.no_connection);

            // Show the dialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getWindow().setLayout(800, 800);
        }
    }

    /**
     * Determines wheter device is connected to internet
     */
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Prepares the travel mode list
     * to be sent to DistanceMatrixAPI
     */
    private List<String> buildTravelModes(float distance) {
        List<String> travelModes = new ArrayList<>();
        if (distance < Constants.DISTANCE_THRESHOLD) {
            travelModes.add(MatrixDistanceApiClient.MODE_DRIVING);
        }
        if (distance < Constants.WALKING_DISTANCE_THRESHOLD) {
            travelModes.add(MatrixDistanceApiClient.MODE_WALKING);
        }
        if (distance < Constants.TRANSIT_DISTANCE_THRESHOLD) {
            travelModes.add(MatrixDistanceApiClient.MODE_TRANSIT);
        }
        return travelModes;
    }
 }
