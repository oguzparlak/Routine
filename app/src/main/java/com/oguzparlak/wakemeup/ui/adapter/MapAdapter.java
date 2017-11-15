package com.oguzparlak.wakemeup.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.utils.BitmapUtils;

/**
 * @author Oguz Parlak
 * <p>
 * Map - user interaction will be handled in
 * this adapter.
 * </p>
 **/

public class MapAdapter {

    /**
     * The current Marker that user points in the map
     */
    private Marker mCurrentMarker;

    /**
     * The circle that covers the marker
     */
    private Circle mCircle;

    /**
     * GoogleMap instance
     */
    private GoogleMap mGoogleMap;

    private Context mContext;

    public MapAdapter(Context context, GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mContext = context;
    }

    /**
     * Marks the given point
     */
    public void markPoint(LatLng latLng) {
        if (mCurrentMarker == null) {
            mCurrentMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .title("")
                    .position(latLng)
                    .zIndex(2)
                    .icon(BitmapUtils.bitmapDescriptorFromVector(mContext, R.drawable.ic_marker_48dp)));
        }

        if (mCircle == null) {
            mCircle = mGoogleMap.addCircle(new CircleOptions()
                .zIndex(2)
                .radius(100)
                .center(latLng)
                .strokeColor(ContextCompat.getColor(mContext, R.color.colorTransparentPrimary))
                .fillColor(ContextCompat.getColor(mContext, R.color.colorTransparentPrimary)));
        }

        mCurrentMarker.setPosition(latLng);
        mCircle.setCenter(latLng);

        // Prepare Camera
        prepareCamera();
    }

    private void prepareCamera() {
        final float currentZoomLevel = mGoogleMap.getCameraPosition().zoom;
        LatLng fixedLatLng = new LatLng(mCurrentMarker.getPosition().latitude - 0.004, mCurrentMarker.getPosition().longitude);
        if (currentZoomLevel < 15) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fixedLatLng, 15));
        } else {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentMarker.getPosition(), currentZoomLevel));
        }
    }

}
