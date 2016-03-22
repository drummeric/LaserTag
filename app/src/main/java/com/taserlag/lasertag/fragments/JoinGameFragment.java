package com.taserlag.lasertag.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.DBGame;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;
import java.util.List;

public class JoinGameFragment extends Fragment{

    private final String TAG = "JoinGameFragment";
    //1.609 KM = 1 Mile
    private final double SEARCH_RADIUS = 1.60934;

    private RecyclerView.Adapter mAdapter;
    private GeoQuery mGeoQuery;

    private List<String> nearbyGames = new ArrayList<>();

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
        init(view);
        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mGeoQuery.removeAllListeners();
    }

    private void init(View view){
        //init GeoQuery w/ bs location within 1 mile
        mGeoQuery = LaserTagApplication.geoFire.queryAtLocation(new GeoLocation(0,0), SEARCH_RADIUS);

        //resets center of GeoQuery
        if (!updateQueryLocation(LocationManager.GPS_PROVIDER)){
            updateQueryLocation(LocationManager.NETWORK_PROVIDER);
        }

        //keeps nearbyGames updated when games are added/deleted
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!nearbyGames.contains(key)) {
                    nearbyGames.add(key);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onKeyExited(String key) {
                nearbyGames.remove(key);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // no op
            }

            @Override
            public void onGeoQueryReady() {
                // no op
            }

            @Override
            public void onGeoQueryError(FirebaseError error) {
                // no op
            }
        });

        //init Recycler
        // uses the list that gets updated by GeoQuery
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_game);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_game,parent,false);
                return new GameViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
                final GameViewHolder holder = (GameViewHolder) viewHolder;
                LaserTagApplication.firebaseReference.child("games").child(nearbyGames.get(position)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final DBGame dbGame = dataSnapshot.getValue(DBGame.class);
                        if (dbGame != null) {
                            LaserTagApplication.firebaseReference.child("games").child(nearbyGames.get(position)).removeEventListener(this);
                            holder.gameName.setText(dbGame.getHost() + "'s Game");
                            holder.gameDescription.setText(dbGame.toString());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Game.getInstance(dbGame, LaserTagApplication.firebaseReference.child("games").child(nearbyGames.get(position)));
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
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public int getItemCount() {
                return nearbyGames.size();
            }
        };
        recycler.setAdapter(mAdapter);
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView gameName;
        TextView gameDescription;

        public GameViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view_game);
            gameName = (TextView)itemView.findViewById(R.id.text_view_game_name);
            gameDescription = (TextView)itemView.findViewById(R.id.text_view_game_description);
        }
    }

    // stores GeoFire Location with key using provider
    // initially uses lastKnownLocation and then updates once when updated location available
    private boolean updateQueryLocation(String provider){
        final LocationManager locationManager = (LocationManager) LaserTagApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        // Request location updates if location permission is granted
        if (ActivityCompat.checkSelfPermission(LaserTagApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(provider)) {

                //set lastKnownLocation as Game location so friends can join immediately
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location!=null) {
                    mGeoQuery.setCenter(new GeoLocation(location.getLatitude(), location.getLongitude()));
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mGeoQuery.setCenter(new GeoLocation(location.getLatitude(), location.getLongitude()));
                        if (ActivityCompat.checkSelfPermission(LaserTagApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.removeUpdates(this);
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                });
                return true;
            }
        }
        return false;
    }
}
