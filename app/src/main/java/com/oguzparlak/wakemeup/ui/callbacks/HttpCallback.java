package com.oguzparlak.wakemeup.ui.callbacks;

import com.google.gson.JsonObject;

/**
 * @author Oguz Parlak
 * <p>
 * This callback will be triggered when Http call is finished
 * </p/
 **/

public interface HttpCallback {
    void onFinished(int statusCode, JsonObject response);
}
