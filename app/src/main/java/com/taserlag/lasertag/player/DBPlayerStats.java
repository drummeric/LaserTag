package com.taserlag.lasertag.player;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.team.Team;

//stores player stats we want to access after game is over
public class DBPlayerStats {

    private static final String TAG = "DBPlayerStats";

    private int score = 0;

    private boolean captain = false;

    private int kills = 0;

    private int deaths = 0;

    private double hitPercentage = 0;

    private int[] color = {255,0,0,0};

    public DBPlayerStats(){}

    public int getScore() {
        return score;
    }

    public boolean isCaptain() {
        return captain;
    }

    public void setCaptain(boolean captain) {
        this.captain = captain;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getHitPercentage() {
        return hitPercentage;
    }

    public void setHitPercentage(double hitPercentage) {
        this.hitPercentage = hitPercentage;
    }

    public int[] getColor() {
        return color;
    }

    public void setColor(int[] color){
        this.color = color;
    }

    //used to set someone else's color
    public static void saveColor(final int[] color, Firebase reference) {
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData playerStatsData) {
                DBPlayerStats playerStats = playerStatsData.getValue(DBPlayerStats.class);
                if (playerStats==null){
                    return Transaction.abort();
                }

                playerStats.setColor(color);
                playerStatsData.setValue(playerStats);

                return Transaction.success(playerStatsData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });
    }

    //increments player and team score
    public void incrementScore(final int value, Firebase reference){
        reference.child("score").runTransaction(new Transaction.Handler() {
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
                Log.i(TAG, "Successfully incremented score for player " + LaserTagApplication.getUid());
                Team.getInstance().incrementScore(value);
            }
        });
    }

    public void incrementDeaths(Firebase reference){
        reference.child("deaths").setValue(++deaths);
    }

    //for now kills has the same value as score
    public void incrementKills(Firebase reference){
        reference.child("kills").setValue(++kills);
    }

    public void saveHitPercentage(Double hitPercentage, Firebase reference) {
        reference.child("hitPercentage").setValue(hitPercentage);
    }
}
