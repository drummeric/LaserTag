package com.taserlag.lasertag.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.taserlag.lasertag.R;

public class MapAssistant implements OnMapReadyCallback, LocationListener {

    private Activity activity;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LatLng lastLoc;
    private static MapAssistant instance;

    public static MapAssistant getInstance(Activity activity){
        if (instance == null) {
            instance = new MapAssistant();
        }

        instance.activity = activity;
        return instance;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        Location location = null;
        // Request location updates if location permission is granted
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }

        // Move map to last known location
        if (location != null){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f);
            this.googleMap.moveCamera(cameraUpdate);
        }

        // Set OnMapClickListener after map is ready
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(activity instanceof MapHandler)
                    ((MapHandler) activity).handleMapClick(latLng);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if(activity instanceof MapHandler)
            ((MapHandler) activity).handleLocChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public void initializeMap() {
        if(activity instanceof FragmentActivity) {
            SupportMapFragment mapFragment = (SupportMapFragment) ((FragmentActivity) activity).getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(instance);
        }
    }

    public void clearGoogleMap(){
        googleMap.clear();
    }

    public void addMarker(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions mp = new MarkerOptions();
        mp.position(latLng).title("Current Location").flat(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.maps_arrow));
        googleMap.addMarker(mp);
    }

    public void animateCamera(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f);
        googleMap.animateCamera(cameraUpdate);
    }

} // MapStuff
