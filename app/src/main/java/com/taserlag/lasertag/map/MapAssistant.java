package com.taserlag.lasertag.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

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

    private final int MAP_SIZE = 125;
    private final int MAP_MARGIN = 10;
    private final int MAP_ANIMATION_DURATION = 100;

    private Activity activity;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private View mMapView;
    private static MapAssistant instance;

    private boolean mapExpanded = false;
    private int mapWidth;
    private int mapHeight;
    private int mapExpandedWidth;
    private int mapExpandedHeight;
    private int mapMargin;

    public static MapAssistant getInstance(Activity activity){
        if (instance == null) {
            instance = new MapAssistant();
        }

        instance.activity = activity;
        return instance;
    }

    public boolean getMapExpanded() {
        return mapExpanded;
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f);
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
            mMapView = (View) mapFragment.getView().getParent();

            // Initialize map layout
            calculateMapDimensions();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mapWidth, mapHeight);
            layoutParams.setMargins(mapMargin, 0, 0, mapMargin);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mMapView.setLayoutParams(layoutParams);
        }
    }

    private void calculateMapDimensions() {
        // Convert MAP_SIZE to px
        Resources r = activity.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAP_SIZE, r.getDisplayMetrics());
        mapWidth = Math.round(px);
        mapHeight = Math.round(px);

        // Get screen metrics
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        // Convert MAP_MARGIN to px
        px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAP_MARGIN, r.getDisplayMetrics());
        mapExpandedWidth = Math.round(metrics.widthPixels - 2*px);
        mapExpandedHeight = Math.round(metrics.heightPixels - 2*px);
        mapMargin = Math.round(px);
    }

    public void maximizeMap() {
        mapExpanded = true;
        ResizeAnimation anim = new ResizeAnimation(mMapView, mapExpandedWidth, mapExpandedHeight);
        anim.setDuration(MAP_ANIMATION_DURATION);
        mMapView.startAnimation(anim);
    }

    public void minimizeMap() {
        mapExpanded = false;
        ResizeAnimation anim = new ResizeAnimation(mMapView, mapWidth, mapHeight);
        anim.setDuration(MAP_ANIMATION_DURATION);
        mMapView.startAnimation(anim);
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
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f);
        googleMap.animateCamera(cameraUpdate);
    }

} // MapStuff