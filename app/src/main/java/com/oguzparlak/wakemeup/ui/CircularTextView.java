package com.oguzparlak.wakemeup.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.oguzparlak.wakemeup.utils.ColorUtils;

import java.util.List;
import java.util.Random;

import static android.R.attr.strokeColor;
import static android.R.attr.strokeWidth;

/**
 * Created by Oguz on 05/07/2017.
 */

public class CircularTextView extends android.support.v7.widget.AppCompatTextView {

    private final int STROKE_COLOR = getRandomColor();
    private final int SOLID_COLOR = getRandomColor();

    private Context mContext;

    public CircularTextView(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint circlePaint = new Paint();
        circlePaint.setColor(ContextCompat.getColor(mContext, SOLID_COLOR));
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        Paint strokePaint = new Paint();
        strokePaint.setColor(ContextCompat.getColor(mContext, STROKE_COLOR));
        strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        int  h = this.getHeight();
        int  w = this.getWidth();

        int diameter = ((h > w) ? h : w);
        int radius = diameter/2;

        this.setHeight(diameter);
        this.setWidth(diameter);

        canvas.drawCircle(diameter / 2 , diameter / 2, radius, strokePaint);

        long strokeWidth = 1;
        canvas.drawCircle(diameter / 2, diameter / 2, radius - strokeWidth, circlePaint);

        super.draw(canvas);
    }

    private static int getRandomColor() {
        Random random = new Random();
        List<Integer> colors = ColorUtils.getTagColors();
        int randIndex = random.nextInt(colors.size());
        return colors.get(randIndex);
    }
}
