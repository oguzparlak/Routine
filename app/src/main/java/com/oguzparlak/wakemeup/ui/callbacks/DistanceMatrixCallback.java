package com.oguzparlak.wakemeup.ui.callbacks;

import com.oguzparlak.wakemeup.model.MatrixDistanceModel;

/**
 * @author Oguz Parlak
 * <p>
 * Callback will be passed from Fragment to host Activity
 * </p>
 **/

public interface DistanceMatrixCallback {
    void onPrepare();
    void onModelReceived(MatrixDistanceModel model);
}
