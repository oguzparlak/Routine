package com.oguzparlak.wakemeup.constants;

import com.oguzparlak.wakemeup.provider.TaskContract;

/**
 * @author Oguz Parlak
 * <p>
 * Constants will be defined here
 * </p/
 **/

public class Constants {

    /**
     * Distance Thresholds in meters
     */
    public static final int WALKING_DISTANCE_THRESHOLD = 5000;
    public static final int TRANSIT_DISTANCE_THRESHOLD = 60000;
    public static final int DISTANCE_THRESHOLD = 80000;

    /**
     * Extras
     */
    public static final String DESTINATION_ADDRESS_EXTRA = "destination_extra";
    public static final String DURATION_EXTRA = "duration_extra";
    public static final String DISTANCE_EXTRA = "distance_extra";

    /**
     * Distance Matrix API KEY
     */
    public static final String KEY_DISTANCE_MATRIX_API = "AIzaSyBu6ekko3_f9DCCcdKLqi-59JNdjABB9bs";

    /**
     * Request Ids
     */
    public static final int EDIT_TASK_REQUEST = 4;

    /**
     * Keys
     */
    public static final String PREFERENCE_ID = "preference_id";

    /**
     * Database
     */
    // Projection, define which columns to return
    public static final String[] PROJECTION =
            {
                    TaskContract.TaskEntry._ID ,
                    TaskContract.TaskEntry.COLUMN_PLACE_ID,
                    TaskContract.TaskEntry.COLUMN_ACTIVE,
                    TaskContract.TaskEntry.COLUMN_TAG,
                    TaskContract.TaskEntry.COLUMN_COLOR,
                    TaskContract.TaskEntry.COLUMN_RADIUS
            };

}
