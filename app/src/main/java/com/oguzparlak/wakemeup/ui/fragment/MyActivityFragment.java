package com.oguzparlak.wakemeup.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.oguzparlak.wakemeup.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Oguz Parlak
 * <p>
 * Users can see their daily activities
 * in this Fragment
 * </p/
 **/
public class MyActivityFragment extends Fragment {

    @BindView(R.id.lottie_animation_view)
    LottieAnimationView mLottieAnimationView;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Lottie
        View rootView = inflater.inflate(R.layout.fragment_my_activity, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
