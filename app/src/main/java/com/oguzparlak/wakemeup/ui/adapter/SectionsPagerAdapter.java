package com.oguzparlak.wakemeup.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.ui.fragment.MapFragment;
import com.oguzparlak.wakemeup.ui.fragment.MyActivityFragment;
import com.oguzparlak.wakemeup.ui.fragment.TaskListFragment;

/**
 * @author Oguz Parlak
 * <p>
 * Adapter of the ViewPager to switch between tabs
 * </p/
 **/

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = SectionsPagerAdapter.class.getSimpleName();

    private Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MapFragment();
            case 1:
                return new TaskListFragment();
            case 2:
                return new MyActivityFragment();
            default:
                return null;
        }
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.map);
            case 1:
                return mContext.getString(R.string.locations);
            case 2:
                return mContext.getString(R.string.activity);
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 3;
    }
}
