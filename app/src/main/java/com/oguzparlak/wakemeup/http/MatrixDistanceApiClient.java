package com.oguzparlak.wakemeup.http;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.oguzparlak.wakemeup.constants.Constants;
import com.oguzparlak.wakemeup.ui.callbacks.HttpCallback;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.TextStyle;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Oguz Parlak
 * <p>
 * This client makes call to Google's Matrix Distance API
 * to calculate the distance and estimated time between two locations
 * It also contains Geocoder API features
 * Sample query:
 * https://maps.googleapis.com/maps/api/distancematrix/json?origins=41.0268405,29.1836654&destinations=41.01566491326365,29.128788895905014&key=AIzaSyBu6ekko3_f9DCCcdKLqi-59JNdjABB9bs
 * </p/
 **/

public class MatrixDistanceApiClient {

    /**
     * Travel Modes
     */
    public static final String MODE_DRIVING = "driving";
    public static final String MODE_WALKING = "walking";
    public static final String MODE_TRANSIT = "transit";

    /**
     * The base URL for the Matrix Distance API
     */
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    /**
     * origin parameter
     */
    private static final String PARAM_ORIGIN = "origins";

    /**
     * destination parameter
     */
    private static final String PARAM_DESTINATION = "destinations";

    /**
     * key parameter
     */
    private static final String PARAM_KEY = "key";

    /**
     * Specifies the travel mode defaults to driving
     */
    private static final String PARAM_MODE = "mode";

    /**
     * Singleton Instance
     */
    private static MatrixDistanceApiClient sInstance;

    /**
     * TAG
     */
    private static final String TAG = MatrixDistanceApiClient.class.getSimpleName();

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private LatLng mOrigin;
    private LatLng mDestination;

    private MatrixDistanceApiClient() {
        /* Private Singleton Constructor */
    }

    public static MatrixDistanceApiClient getInstance() {
        if (sInstance == null) {
            sInstance = new MatrixDistanceApiClient();
        }
        return sInstance;
    }

    public void makeCall(String mode, HttpCallback httpCallback) {
        Request request = new Request.Builder()
                .url(buildUrl(mOrigin, mDestination, mode))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: e: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    JsonParser parser = new JsonParser();
                    httpCallback.onFinished(response.code(),
                            parser.parse(responseBody.string()).getAsJsonObject());
                }
            }
        });
    }

    public void setSource(LatLng latLng) {
        mOrigin = latLng;
    }

    public void setDestination(LatLng latLng) {
        mDestination = latLng;
    }

    private String buildUrl(LatLng origin, LatLng destination, String travelMode) {
        Uri.Builder builder = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter(PARAM_ORIGIN, buildLatLng(origin))
                .appendQueryParameter(PARAM_DESTINATION, buildLatLng(destination))
                .appendQueryParameter(PARAM_KEY, Constants.KEY_DISTANCE_MATRIX_API);
        if (!(travelMode == null || TextUtils.isEmpty(travelMode))) {
            builder.appendQueryParameter(PARAM_MODE, travelMode);
        }
        Uri uri = builder.build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        if (url != null) {
            return url.toString();
        } else {
            throw new IllegalArgumentException("URL couldn't be generated");
        }
    }

    private String buildLatLng(LatLng bounds) {
        return new StringBuilder()
                .append(bounds.latitude)
                .append(",").append(bounds.longitude).toString();
    }

}
