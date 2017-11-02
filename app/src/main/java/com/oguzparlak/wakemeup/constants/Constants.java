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
