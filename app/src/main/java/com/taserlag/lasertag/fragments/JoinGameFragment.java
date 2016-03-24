package com.taserlag.lasertag.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.DBGame;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;

import java.util.HashMap;
import java.util.Map;

public class JoinGameFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "JoinGameFragment";
    private final int INITIAL_ZOOM = 15;

    private Map<String, Marker> gameMarkers = new HashMap<>();

    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;

    public JoinGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_game, container, false);
        //just in case
        Game.getInstance().leaveGame();
        init();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (mMapFragment == null) {
                mMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_join_game));
                getChildFragmentManager().beginTransaction().add(mMapFragment, "join_game_map").commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            getChildFragmentManager().beginTransaction().remove(mMapFragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapFragment = null;
        gameMarkers.clear();
    }

    private void init() {
        mMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_join_game));
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {

                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());

                info.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle().split(":~")[0]);

                TextView description = new TextView(getContext());
                description.setTextColor(Color.GRAY);
                description.setText(marker.getSnippet());

                info.addView(title);
                info.addView(description);

                return info;
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LaserTagApplication.firebaseReference.child("games").child(marker.getTitle().split(":~")[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DBGame dbGame = dataSnapshot.getValue(DBGame.class);

                        Game.getInstance(dbGame, dataSnapshot.getRef());
                        DBTeam foundTeam = Game.getInstance().findPlayer(Player.getInstance().getName());
                        if (Game.getInstance().isGameOver()) {
                            Toast.makeText(LaserTagApplication.getAppContext(), "This game is over!", Toast.LENGTH_SHORT).show();
                        } else if (Game.getInstance().isGameReady()) {
                            if (foundTeam != null) {
                                Team.getInstance(foundTeam);
                                Player.getInstance().join();
                                ((MenuActivity) getActivity()).launchFPS();
                            } else {
                                Toast.makeText(LaserTagApplication.getAppContext(), "This game has started already!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, GameLobbyFragment.newInstance(), "game_lobby_fragment");
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        });

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //please no
            return;
        }

        //centers camera on Gainesville
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        if (!centerMapOnMyLocation(LocationManager.GPS_PROVIDER)){
            centerMapOnMyLocation(LocationManager.NETWORK_PROVIDER);
        }

        //adds marks
        LaserTagApplication.firebaseReference.child("gameLocations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!gameMarkers.containsKey(dataSnapshot.getKey())) {
                    double lat = dataSnapshot.child("l").child("0").getValue(Double.class);
                    double lng = dataSnapshot.child("l").child("1").getValue(Double.class);
                    addGameMarker(dataSnapshot.getKey(), lat, lng);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (gameMarkers.containsKey(dataSnapshot.getKey())) {
                    double lat = dataSnapshot.child("l").child("0").getValue(Double.class);
                    double lng = dataSnapshot.child("l").child("1").getValue(Double.class);
                    gameMarkers.get(dataSnapshot.getKey()).setPosition(new LatLng(lat, lng));
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (gameMarkers.containsKey(dataSnapshot.getKey())) {
                    gameMarkers.remove(dataSnapshot.getKey()).remove();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //no op
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void addGameMarker(final String gameKey, double lat, double lng){
        final LatLng latLng = new LatLng(lat, lng);
        final MarkerOptions mp = new MarkerOptions();

        LaserTagApplication.firebaseReference.child("games").child(gameKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DBGame dbGame = dataSnapshot.getValue(DBGame.class);
                if (dbGame != null) {
                    int resource;

                    if (!gameMarkers.containsKey(gameKey)) {
                        if (dbGame.isGameReady()) {
                            resource = R.drawable.gamemapiconinprogress;
                            LaserTagApplication.firebaseReference.child("games").child(gameKey).removeEventListener(this);
                        } else {
                            resource = R.drawable.gamemapicon;
                        }

                        mp.title(dbGame.getGameType().toString() + " Game:~" + gameKey).snippet(dbGame.toString());
                        mp.position(latLng).flat(true).icon(BitmapDescriptorFactory.fromResource(resource));
                        gameMarkers.put(gameKey, mGoogleMap.addMarker(mp));

                    } else {
                        if (dbGame.isGameReady()) {
                            resource = R.drawable.gamemapiconinprogress;
                            LaserTagApplication.firebaseReference.child("games").child(gameKey).removeEventListener(this);
                        } else {
                            resource = R.drawable.gamemapicon;
                        }

                        gameMarkers.get(gameKey).setIcon(BitmapDescriptorFactory.fromResource(resource));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private boolean centerMapOnMyLocation(String provider){
        final LocationManager locationManager = (LocationManager) LaserTagApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        // Request location updates if location permission is granted
        if (ActivityCompat.checkSelfPermission(LaserTagApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(provider)) {

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    CameraUpdate centerMe = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), INITIAL_ZOOM);
                    mGoogleMap.animateCamera(centerMe);
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        CameraUpdate centerMe = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), INITIAL_ZOOM);
                        mGoogleMap.animateCamera(centerMe);
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            locationManager.removeUpdates(this);
                        }
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
                });
                return true;
            }
        }
        return false;
    }
}
