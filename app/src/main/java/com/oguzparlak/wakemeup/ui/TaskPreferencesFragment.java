package com.oguzparlak.wakemeup.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.oguzparlak.wakemeup.R;

/**
 * Created by Oguz on 10/07/2017.
 */

public class TaskPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load Preferences from XML file
        addPreferencesFromResource(R.xml.task_preferences);
    }



}
