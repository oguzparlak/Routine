package com.oguzparlak.wakemeup.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.provider.TaskContract;
import com.oguzparlak.wakemeup.ui.ListItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskRecyclerAdapter extends RecyclerViewCursorAdapter<TaskRecyclerAdapter.TaskViewHolder>{

    private Context mContext;
    private ListItemClickListener mListItemClickListener;

    public TaskRecyclerAdapter(Cursor cursor, Context context, ListItemClickListener listItemClickListener) {
        super(cursor);
        mContext = context;
        mListItemClickListener = listItemClickListener;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, Cursor cursor) {
        final int adapterPosition = holder.getAdapterPosition();

        cursor.moveToPosition(adapterPosition);

        String placeName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TAG));
        String radius = String.valueOf(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_RADIUS)));
        boolean activated = (cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_ACTIVE)) == 1);
        int color = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_COLOR));
        final int id = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID));

        holder.mPlaceTextView.setText(placeName);
        holder.mRadiusTextView.setText(radius + " km");
        holder.mSwitch.setChecked(activated);
        holder.mTagTextView.setText(placeName.substring(0, 1));

        // Set the background of TextView
        ShapeDrawable circleDrawable= new ShapeDrawable(new OvalShape());
        circleDrawable.setIntrinsicHeight(40);
        circleDrawable.setIntrinsicWidth(40);
        circleDrawable.setBounds(new Rect(20, 20, 20, 20));
        circleDrawable.getPaint().setColor(color);
        holder.mTagTextView.setBackground(circleDrawable);

        // This will be triggered when a row is clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListItemClickListener.onItemClicked(view, adapterPosition, id);
            }
        });

        // This block will be triggered when user interacts with switch
        holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mListItemClickListener.onCheckChanged(isChecked, id);
            }
        });
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.saved_locations_list_item, parent, false);
        return new TaskViewHolder(v);
    }

    /**
     * ViewHolder class contains the visual components of a Task
     */
    static final class TaskViewHolder extends RecyclerView.ViewHolder {

        // ButterKnife Binding
        @BindView(R.id.geofence_enabled_switch)
        Switch mSwitch;
        @BindView(R.id.list_item_place_name_text)
        TextView mPlaceTextView;
        @BindView(R.id.list_item_radius_text)
        TextView mRadiusTextView;
        @BindView(R.id.list_item_tag_image)
        TextView mTagTextView;

        TaskViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
