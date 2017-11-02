package com.oguzparlak.wakemeup.ui.callbacks;

import com.google.android.gms.location.places.Place;

/**
 * @author Oguz Parlak
 * <p>
 * Callback fires when a Google Place is selected from AutoCompleteWidget
 * </p/
 **/

public interface GooglePlaceSelectionListener {
    void onPlaceSelected(Place place);
}
