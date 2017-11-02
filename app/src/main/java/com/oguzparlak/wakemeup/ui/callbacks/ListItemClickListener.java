package com.oguzparlak.wakemeup.ui.callbacks;

import android.view.View;

public interface ListItemClickListener {
    void onItemClicked(View v, int position, int id);
    void onCheckChanged(boolean checked, int id);
}

