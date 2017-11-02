package com.oguzparlak.wakemeup.ui.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import com.oguzparlak.wakemeup.provider.TaskContract;

public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;
    private boolean mDataValid;

    public abstract void onBindViewHolder(VH holder, Cursor cursor);

    RecyclerViewCursorAdapter(Cursor cursor) {
        setHasStableIds(true);
        swapCursor(cursor);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Can't bind to ViewHolder when data is invalid");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Can't bind to ViewHolder, mCursor out of bounds");
        }

        onBindViewHolder(holder, mCursor);
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
             return mCursor.getLong(mCursor.getColumnIndex(TaskContract.TaskEntry._ID));
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        return mCursor != null && mDataValid ? mCursor.getCount() : 0;
    }

    public void swapCursor(Cursor cursor) {
        if (this.mCursor == cursor) {
            return;
        }

        if (cursor != null) {
            this.mCursor = cursor;
            mDataValid = true;
            // Notify the observers
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            this.mCursor = null;
            mDataValid = false;
        }
    }

}
