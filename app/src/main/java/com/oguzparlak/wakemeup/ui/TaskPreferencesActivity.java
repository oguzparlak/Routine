package com.oguzparlak.wakemeup.ui;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.constants.Constants;

public class TaskPreferencesActivity extends AppCompatActivity {

    public static final String PLACE_NAME_EXTRA = "place_name_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_preferences);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getStringExtra(PLACE_NAME_EXTRA));
        }

        // Init PrefrenceFragment
        TaskPreferencesFragment taskPreferencesFragment = new TaskPreferencesFragment();

        // Set arguments
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PREFERENCE_ID, getIntent().getStringExtra(Constants.PREFERENCE_ID));
        taskPreferencesFragment.setArguments(bundle);

        // Attach fragment to Activity
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.preference_fragment_holder, taskPreferencesFragment)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
