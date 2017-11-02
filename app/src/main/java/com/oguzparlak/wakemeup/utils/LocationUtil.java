package com.oguzparlak.wakemeup.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author Oguz Parlak
 * <p>
 * Helper class to perform Location related operations
 * </p/
 **/
public class LocationUtil {

    private Location mLocation;

    public LocationUtil(Location location) {
        mLocation = location;
    }

    public LatLng getLatLng() {
        return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    public float getDistanceTo(Location anotherLocation) {
        return mLocation.distanceTo(anotherLocation);
    }
}
