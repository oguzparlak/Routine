package com.oguzparlak.wakemeup.utils;

import com.google.android.gms.location.GeofencingRequest;

/**
 * @author Oguz Parlak
 * <p>
 * Helper method that builds a Geofence
 * </p>
 **/

public class GeofenceBuilder {

    // TODO
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        return null;
    }

}
