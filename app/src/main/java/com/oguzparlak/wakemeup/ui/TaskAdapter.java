package com.oguzparlak.wakemeup.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.provider.TaskContract;
import com.oguzparlak.wakemeup.provider.TaskProvider;
import com.oguzparlak.wakemeup.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Created by Oguz on 06/07/2017.
 */

class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    
    private static final String TAG = TaskAdapter.class.getSimpleName();

    private Cursor mCursor;
    private RecyclerViewOnClickListener mClickListener;
    private RecyclerViewOnCheckedChangedListener mCheckedChangeListener;
    private Context mContext;

    TaskAdapter(Context context,
                RecyclerViewOnClickListener clickListener,
                RecyclerViewOnCheckedChangedListener checkedChangedListener) {
        mClickListener = clickListener;
        mCheckedChangeListener = checkedChangedListener;
        mContext = context;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.saved_locations_list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String placeName = mCursor.getString(mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TAG));
        String radius = String.valueOf(mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_RADIUS)));
        boolean activated = (mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_ACTIVE)) == 1);
        int color = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_COLOR));
        final int id = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskEntry._ID));

        holder.mPlaceTextView.setText(placeName);
        holder.mRadiusTextView.setText(radius + " km");
        holder.mSwitch.setChecked(activated);
        holder.mTagTextView.setText(placeName.substring(0, 1));

        // Set the background of TextView
        ShapeDrawable circleDrawable= new ShapeDrawable(new OvalShape());
        circleDrawable.setIntrinsicHeight(40);
        circleDrawable.setIntrinsicWidth(40);
        circleDrawable.setBounds(new Rect(20, 20, 20, 20));
        circleDrawable.getPaint().setColor(color);//you can give any color here
        holder.mTagTextView.setBackground(circleDrawable);

        final int adapterPosition = holder.getAdapterPosition();

        // This will be triggered when a row is clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onItemClicked(view, adapterPosition, id);
            }
        });

        // This will be triggered when switch is clicked
        holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mCheckedChangeListener.onCheckChanged(checked, id);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        // ButterKnife Binding
        @BindView(R.id.geofence_enabled_switch) Switch mSwitch;
        @BindView(R.id.list_item_place_name_text) TextView mPlaceTextView;
        @BindView(R.id.list_item_radius_text) TextView mRadiusTextView;
        @BindView(R.id.list_item_tag_image) TextView mTagTextView;

        TaskViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
