package com.oguzparlak.wakemeup.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TaskContract {

    // A string that identifies the entire content provider
    public static final String AUTHORITY = "com.oguzparlak.wakemeup";

    // Base of the complete Uri. content://<authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Path for the tasks directory
    public static final String PATH_TASKS = "tasks";

    public static final class TaskEntry implements BaseColumns {

        // Complete Uri
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        public static final String TABLE_NAME = "tasks";

        // Table Columns
        public static final String COLUMN_PLACE_ID = "place_id";
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_RADIUS = "radius";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_COLOR = "color";
    }
}
