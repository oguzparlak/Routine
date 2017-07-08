package com.oguzparlak.wakemeup.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.oguzparlak.wakemeup.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Oguz on 05/07/2017.
 */

public class ColorUtils {

    public static List<Integer> getTagColors() {
        List<Integer> colors = new ArrayList<>();
        colors.add(R.color.colorTagBlue);
        colors.add(R.color.colorTagBrown);
        colors.add(R.color.colorTagCyan);
        colors.add(R.color.colorTagDarkGrey);
        colors.add(R.color.colorTagGreen);
        colors.add(R.color.colorTagGrey);
        colors.add(R.color.colorTagLime);
        colors.add(R.color.colorTagOrange);
        colors.add(R.color.colorTagPurple);
        colors.add(R.color.colorTagRed);
        colors.add(R.color.colorTagTeal);
        return colors;
    }

    public static int getRandomColor(Context context) {
        Random random = new Random();
        int randIndex = random.nextInt(getTagColors().size());
        return ContextCompat.getColor(context, getTagColors().get(randIndex));
    }

}
