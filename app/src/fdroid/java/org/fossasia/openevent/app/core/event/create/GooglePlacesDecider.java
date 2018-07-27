package org.fossasia.openevent.app.core.event.create;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

public class GooglePlacesDecider {

    private final double DEMO_VALUE = 1;

    public void onSelectingButtonPlacePicker(Activity activity) {
        //do nothing
    }

    @SuppressLint("RestrictedApi")
    public void setGooglePlaces(Activity activity, Intent data) {
        //do nothing
    }

    public boolean shouldShowLocationLayout() {
        return true;
    }

    public double getLatitude() {
        return DEMO_VALUE;
    }

    public double getLongitude() {
        return DEMO_VALUE;
    }

    public CharSequence getAddress() {
        return null;
    }
}
