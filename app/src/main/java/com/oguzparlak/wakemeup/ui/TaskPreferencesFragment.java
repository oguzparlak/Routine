package com.oguzparlak.wakemeup.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.constants.Constants;

public class TaskPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Set the id for each task, this will provide seperate preferences for each task
        getPreferenceManager().setSharedPreferencesName(getArguments().getString(Constants.PREFERENCE_ID));
        // Load Preferences from XML file
        addPreferencesFromResource(R.xml.task_preferences);
    }



}
