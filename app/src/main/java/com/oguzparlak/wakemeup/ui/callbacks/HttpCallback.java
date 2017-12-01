package com.oguzparlak.wakemeup.ui.callbacks;

import com.google.gson.JsonObject;

import org.json.JSONObject;

/**
 * @author Oguz Parlak
 * <p>
 * This callback will be triggered when Http call is finished
 * </p/
 **/

public interface HttpCallback {
    void onFinished(String travelMode, int statusCode, JsonObject response);
    void onError(int errorCode);
}
