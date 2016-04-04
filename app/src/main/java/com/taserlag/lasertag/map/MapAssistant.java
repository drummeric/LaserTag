package com.taserlag.lasertag.map;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapAssistant implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private final int MAP_SIZE = 125;
    private final int MAP_MARGIN = 10;
    private final int MAP_ANIMATION_DURATION = 100;
    private final long REFRESH_TIME = 5000L;

    private Activity activity;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private View mMapView;
    private Timer refreshTimer;
    private static MapAssistant instance;

    private boolean mapExpanded = false;
    private int mapWidth;
    private int mapHeight;
    private int mapExpandedWidth;
    private int mapExpandedHeight;
    private int mapMargin;

    private Map<String, Marker> markers = new HashMap<>();

    public MapAssistant(Activity activity){
        this.activity = activity;
        initLocation();
    }

    public static MapAssistant getInstance(Activity activity){
        if (instance == null) {
            instance = new MapAssistant(activity);
        }

        instance.activity = activity;

        return instance;
    }

    public boolean getMapExpanded() {
        return mapExpanded;
    }

    public void onPause() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    public void onResume() {
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Set OnMapClickListener after map is ready
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(activity instanceof MapHandler)
                    ((MapHandler) activity).handleMapClick(latLng);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (mapExpanded) {
                    minimizeMap();
                } else {
                    maximizeMap();
                }
                return true;
            }
        });

        final Handler handler = new Handler();
        refreshTimer = new Timer();
        TimerTask mapRefreshTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        refreshMap();
                    }
                });
            }
        };
        refreshTimer.schedule(mapRefreshTask, 0, REFRESH_TIME); //execute in every 15000 ms
    }

    private void initLocation(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(LaserTagApplication.getAppContext())
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
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

    public void cleanup(){
        refreshTimer.cancel();
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
        if (googleMap!=null) {
            googleMap.clear();
        }
        markers.clear();
    }

    public void refreshMap(){
        TeamIterator<DBPlayer> iterator = Game.getInstance().makeIterator();

        while (iterator.hasNext()){
            DBPlayer player = iterator.next();

            if (!player.getName().equals(Player.getInstance().getName()) && player.getLongitude() != 0 && player.getLatitude() != 0) {
                if (Team.getInstance().getName().equals(iterator.currentTeam())) {
                    updateOtherMarker(player.getName(), player.getLatitude(), player.getLongitude(), R.drawable.map_marker_team);
                } else {
                    updateOtherMarker(player.getName(), player.getLatitude(), player.getLongitude(), R.drawable.map_marker_enemy);
                }
            }
        }
    }

    public void updateOtherMarker(String name, double lat, double lng, int drawableID){
        Marker myMarker = markers.get(name);
        if (myMarker == null){
            addMarker(name, lat, lng, drawableID);
        } else {
            myMarker.setPosition(new LatLng(lat, lng));
        }
    }

    public void updateMyMarker(double lat, double lng){
        Marker myMarker = markers.get(Player.getInstance().getName());
        if (myMarker == null){
            addMarker(Player.getInstance().getName(), lat, lng, R.drawable.map_marker_me);
        } else {
            myMarker.setPosition(new LatLng(lat, lng));
        }
    }

    private void addMarker(String name, double lat, double lng, int resource){
        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions mp = new MarkerOptions();

        mp.position(latLng).flat(true).icon(BitmapDescriptorFactory.fromResource(resource));
        markers.put(name, googleMap.addMarker(mp));
    }

    public void animateCamera(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f);
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(activity instanceof MapHandler)
            ((MapHandler) activity).handleLocChanged(location);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapAssistant.this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //no op
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(activity, "Connection failed. Please try again.", Toast.LENGTH_SHORT).show();
    }
} // MapStuff