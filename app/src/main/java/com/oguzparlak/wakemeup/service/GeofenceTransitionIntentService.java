package com.oguzparlak.wakemeup.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.GeofencingEvent;

/**
 * @author Oguz Parlak
 * <p>
 * Receives data from the GeofencingEvents
 * </p>
 **/

public class GeofenceTransitionIntentService extends IntentService {

    public GeofenceTransitionIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            return;
        }

        if (geofencingEvent.hasError()) {
            // Handle error
        }

        // Get transition type
        int transitionType = geofencingEvent.getGeofenceTransition();

        // ...
        // TODO
    }
}
