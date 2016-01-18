package com.taserlag.lasertag.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface MapHandler {

    public void handleMapClick(LatLng latLng);

    public void handleLocChanged(Location location);
}
