package com.taserlag.lasertag.map;

import android.location.Location;

public interface LocationCallback {

    void notifyLocationUpdated(Location location);
}
