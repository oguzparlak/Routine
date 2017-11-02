package com.oguzparlak.wakemeup.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oguzparlak.wakemeup.R;
import com.oguzparlak.wakemeup.constants.Constants;
import com.oguzparlak.wakemeup.provider.TaskContract;
import com.oguzparlak.wakemeup.ui.callbacks.ListItemClickListener;
import com.oguzparlak.wakemeup.ui.callbacks.TaskListFragmentCallbacks;
import com.oguzparlak.wakemeup.ui.adapter.TaskRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Oguz Parlak
 * <p>
 * TaskListFragment contains a RecyclerView to display Tasks to user
 * Users can enable or disable the Task by interacting the switch
 * Users will be able to edit the Task after selecting a row
 * </p/
 **/
public class TaskListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TaskListFragmentCallbacks {

    private static final int LOADER_ID = 1;

    private static final String TAG = TaskListFragment.class.getSimpleName();

    @BindView(R.id.saved_locations_recycler) RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.default_message_text_view) TextView mDefaultText;

    // ButterKnife Unbinder
    private Unbinder mUnbinder;

    // RecyclerView Adapter
    private TaskRecyclerAdapter mTaskAdapter;

    private ListItemClickListener mClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mClickListener = (ListItemClickListener) context;
        } catch (Exception ex) {
            Log.e(TAG, "onAttach: Error occurred when casting");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_task_list, container, false);
        mUnbinder = ButterKnife.bind(this, v);

        // Start the loader
        getLoaderManager().initLoader(LOADER_ID, null, this);

        // Configure RecyclerView Layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration decoration =
                new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(decoration);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    /**
     * LoaderCallbacks
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                TaskContract.TaskEntry.CONTENT_URI,
                Constants.PROJECTION,
                null,
                null,
                TaskContract.TaskEntry.COLUMN_ACTIVE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Set the adapter
        if (mTaskAdapter == null) {
            mTaskAdapter = new TaskRecyclerAdapter(getContext(), mClickListener);
            mRecyclerView.setAdapter(mTaskAdapter);
        }

        // Swap cursor each time Cursor has modified
        mTaskAdapter.swapCursor(cursor);

        // Show default text message when there
        // is no location is added
        if (mTaskAdapter.getItemCount() > 0)
            mDefaultText.setVisibility(View.INVISIBLE);
        else
            mDefaultText.setVisibility(View.VISIBLE);

        // Refresh the views after fab click
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTaskAdapter.swapCursor(null);
    }

    @Override
    public void onFabClicked() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlacePickerDismissed() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}
