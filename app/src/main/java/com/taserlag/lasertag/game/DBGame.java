package com.taserlag.lasertag.game;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.geofire.GeoLocation;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBGame{

    private String host;

    private GameType gameType;

    private boolean scoreEnabled;

    private int endScore;

    private boolean timeEnabled;

    private int endMinutes;

    private int maxTeamSize;

    private int totalScore = 0;

    private boolean friendlyFire;

    private boolean gameReady = false;

    private boolean gameLoaded = false;

    private boolean gameOver = false;

    private Map<String, DBTeam> teams = new HashMap<>();

    private Date date;

    public DBGame() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public boolean getScoreEnabled() {
        return scoreEnabled;
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        this.scoreEnabled = scoreEnabled;
    }

    public int getEndScore() {
        return endScore;
    }

    public void setEndScore(int score) {
        this.endScore = score;
    }

    public boolean getTimeEnabled() {
        return timeEnabled;
    }

    public void setTimeEnabled(boolean timeEnabled) {
        this.timeEnabled = timeEnabled;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(int minutes) {
        this.endMinutes = minutes;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int size) {
        this.maxTeamSize = size;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public boolean getFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public boolean isGameReady() {
        return gameReady;
    }

    private void setReady(boolean ready) {
        this.gameReady = ready;
    }

    public void resetReady(Firebase reference) {
        reference.child("gameReady").setValue(false);
    }

    public boolean isGameLoaded() {
        return gameLoaded;
    }

    private void setLoaded(){
        gameLoaded = true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Map<String, DBTeam> getTeams() {
        return teams;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    //sets ready if its teams are ready
    public void checkReady(final Firebase reference){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DBGame dbGame = mutableData.getValue(DBGame.class);
                boolean ready = true;
                if (dbGame != null) {
                    for (DBTeam dbTeam : dbGame.getTeams().values()) {
                        ready &= dbTeam.isReady();
                    }

                    dbGame.setReady(ready);

                    mutableData.setValue(dbGame);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {

            }
        });
    }

    //sets loaded if its teams are loaded
    public void checkLoaded(final Firebase reference){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DBGame dbGame = mutableData.getValue(DBGame.class);
                boolean loaded = true;
                if (dbGame!=null){
                    for (DBTeam dbTeam: dbGame.getTeams().values()){
                        loaded &= dbTeam.isLoaded();
                    }

                    if (loaded){
                        dbGame.setLoaded();
                    } else {
                        return Transaction.abort();
                    }

                    mutableData.setValue(dbGame);

                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {

            }
        });
    }

    //increment running game score to keep scoreboard updated
    public void incrementTotalScore(final int value, final Firebase reference) {
        reference.child("totalScore").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue(Integer.class) == null) {
                    mutableData.setValue(0);
                } else {
                    mutableData.setValue(mutableData.getValue(Integer.class) + value);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });
    }

    //adds new team to game and puts me on it
    // updates static Team class with new DBTeam
    // updates User's activeGameKey
    public boolean createTeamWithPlayer(final DBTeam dbTeam, final Firebase reference) {

        reference.child("teams").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Map<String, DBTeam> dbTeams = currentData.getValue(Map.class);
                if (dbTeams == null) {
                    //should never happen
                    dbTeams = new HashMap<>();
                }
                dbTeams.put(dbTeam.getName(), dbTeam);

                currentData.setValue(dbTeams);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //adds me to the team
                dbTeam.addDBPlayer(reference.child("teams").child(dbTeam.getName()));
            }
        });

        return true;
    }

    public void saveGameOver(Firebase reference) {
        reference.child("gameOver").setValue(true);
    }

    public Firebase saveNewGame() {
        final Firebase ref = LaserTagApplication.firebaseReference.child("games").push();
        ref.setValue(this);

        if (!storeLocation(ref.getKey(), LocationManager.GPS_PROVIDER)){
            storeLocation(ref.getKey(),LocationManager.NETWORK_PROVIDER);
        }
        return ref;
    }

    // stores GeoFire Location with key using provider
    // initially uses lastKnownLocation and then updates once when updated location available
    private boolean storeLocation(final String key, String provider){
        final LocationManager locationManager = (LocationManager) LaserTagApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        // Request location updates if location permission is granted
        if (ActivityCompat.checkSelfPermission(LaserTagApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(provider)) {

                //set lastKnownLocation as Game location so friends can join immediately
                Location location = locationManager.getLastKnownLocation(provider);
                if (location!=null) {
                    LaserTagApplication.geoFire.setLocation(key, new GeoLocation(location.getLatitude(), location.getLongitude()));
                }

                locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (Game.getInstance().getReference()!=null && Game.getInstance().getKey().equals(key)) {
                            //update the Game location to real current location when available and close listener so only store once
                            LaserTagApplication.geoFire.setLocation(key, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        }
                        if (ActivityCompat.checkSelfPermission(LaserTagApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public String toString(){
        StringBuilder description = new StringBuilder();

        description.append(getHost()).append("'s ");

        description.append(getGameType()).append(" game ");

        if (getScoreEnabled()) {
            description.append("to ").append(getEndScore()).append(" points");
        }

        if (getTimeEnabled()) {
            if(getScoreEnabled()) {
                description.append(" or ");
            }
            description.append("until ").append(getEndMinutes()).append(" minutes have elapsed");
        }

        description.append(". Friendly fire is ");

        if (getFriendlyFire()) {
            description.append("enabled.");
        } else {
            description.append("disabled.");
        }

        if (!getGameType().equals(GameType.FFA)){
            description.append(" The maximum team size is ").append(getMaxTeamSize()).append(".");
        }

        return description.toString();
    }

    public TeamIterator<DBPlayer> makeIterator(){
        return new TeamIterator<DBPlayer>() {
            Iterator<DBPlayer> currentIterator;
            String currentTeamName;
            Iterator<DBTeam> teamIterator = teams.values().iterator();

            @Override
            public boolean hasNext() {
                //only happens first time
                if (currentIterator==null){
                    DBTeam currentTeam = teamIterator.next();
                    currentTeamName = currentTeam.getName();
                    currentIterator = currentTeam.makeIterator();
                }

                if (!currentIterator.hasNext()){
                    if (teamIterator.hasNext()){
                        DBTeam currentTeam = teamIterator.next();
                        currentTeamName = currentTeam.getName();
                        currentIterator = currentTeam.makeIterator();
                    } else {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public DBPlayer next() {
                return currentIterator.next();
            }

            @Override
            public String currentTeam(){
                return currentTeamName;
            }
        };
    }

    // finds player in game (call as little as possible)
    // returns the team of player found
    public DBTeam findPlayer(String name){
        for (Map.Entry<String, DBTeam> teamEntry:teams.entrySet()){
            for (String playerName:teamEntry.getValue().getPlayers().keySet()){
                if (playerName.equals(name)){
                    return teamEntry.getValue();
                }
            }
        }

        return null;
    }
}
