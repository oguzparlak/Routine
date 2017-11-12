package com.oguzparlak.wakemeup.utils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oguz Parlak
 * <p>
 * Helper method that builds a Geofence
 * </p>
 **/

public class GeofenceBuilder {

    // TODO Geofence list should be created according
    // TODO to values which read from the database
    private List<Geofence> mGeofenceList = new ArrayList<>();

    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /**
     * Creates a new Geofence object from LatLng
     * and adds it into list
     */
    public void addGeofence(LatLng latLng) {
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("1")
                .setCircularRegion(latLng.latitude, latLng.longitude, 100)
                .setLoiteringDelay(1000 * 60 * 60 * 12)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                       Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }



}
