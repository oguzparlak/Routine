package com.oguzparlak.wakemeup.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.ui.activity.MainActivity;
import com.oguzparlak.wakemeup.ui.callbacks.GooglePlaceSelectionListener;
import com.oguzparlak.wakemeup.utils.BitmapUtils;
import com.oguzparlak.wakemeup.utils.LocationUtil;

import java.security.Permission;

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

    @BindView(R.id.map_view)
    MapView mGoogleMapView;

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

        LatLng mountainView = new LatLng(37.4, -122.1);

        Marker marker = mGoogleMap.addMarker(
                new MarkerOptions()
                        .position(mountainView)
                        .title("Mountain View")
                        .icon(BitmapUtils.bitmapDescriptorFromVector(getContext(), R.drawable.ic_marker_48dp))
        );

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

        if (mLastKnownLocation != null) {
            Log.d(TAG, "onMapClick: Distance between two points: " + mLocationUtil.getDistanceTo(destination));
        }

        // Put a marker
        Marker marker = putMarker(latLng);

        final float currentZoomLevel = mGoogleMap.getCameraPosition().zoom;
        if (currentZoomLevel < 15) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
        } else {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), currentZoomLevel));
        }

    }

    private Marker putMarker(LatLng latLng) {
        return mGoogleMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title("Mountain View")
                        .icon(BitmapUtils.bitmapDescriptorFromVector(getContext(), R.drawable.ic_marker_48dp))
        );
    }
}
