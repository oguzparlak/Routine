package com.oguzparlak.wakemeup.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Oguz Parlak
 * <p>
 * Custom ViewPager disables user to swipe
 * between pages
 * </p/
 **/

public class NonSwipeableWiewPager extends ViewPager {

    public NonSwipeableWiewPager(Context context) {
        super(context);
    }

    NonSwipeableWiewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
