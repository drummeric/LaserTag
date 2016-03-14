package com.taserlag.lasertag.player;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.team.Team;
// player stuff stored in DB for use IN game (not after game)
public class DBPlayer{

    private static final String TAG = "DBPlayer";

    private String name;

    private int health = 100;

    private boolean ready = false;

    private boolean loaded = false;

    private DBPlayerStats playerStats = new DBPlayerStats();

    public DBPlayer() {}

    public DBPlayer(String name) {
        this.name = name;
        playerStats.setColor(Player.getInstance().getColor());
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public void loadUp(Firebase reference){
        loaded = true;
        reference.child("loaded").setValue(loaded);

        Team.getInstance().checkLoaded();
    }

    public void readyUp(Firebase reference) {
        ready = true;
        reference.child("ready").setValue(ready);
        Team.getInstance().checkReady();
    }

    public DBPlayerStats getPlayerStats() {
        return playerStats;
    }

    public void resetReady(Firebase reference){
        ready = false;
        Team.getInstance().resetReady();
        reference.child("ready").setValue(ready);
    }

    //can only increment my score, does not take in playerUID
    private void incrementScore(final int value, Firebase reference){
        playerStats.incrementScore(value, reference.child("playerStats"));
    }

    public void resetHealth(Firebase reference){
       reference.child("health").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue(Integer.class)!=null){
                    mutableData.setValue(100);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.i(TAG, "Successfully reset health for player " + LaserTagApplication.getUid());
            }
        });
    }

    public void incrementHealth(final int value, Firebase reference){
        // adds maxShieldStrength to health in DB
        reference.child("health").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData healthData) {
                if (healthData.getValue(Integer.class) != null) {
                    healthData.setValue(healthData.getValue(Integer.class) + value);
                }
                return Transaction.success(healthData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    // decrement other people's health, cannot hurt yourself
    // returns false if you try to decrement your own health
    // reference points to DBPlayer in Team's player map
    // my reference points to me in that map
    public boolean decrementHealthAndIncMyScore(final int value,final Firebase reference, final Firebase myReference){
        //since team has a map of player names to dbPlayers, and reference is a reference to the DB player,
        // reference.getKey() should return the player name.
        boolean hitYourself = Player.getInstance().getName().equals(reference.getKey());

        if (!hitYourself) {
            reference.child("health").runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue(Integer.class) == null) {
                        mutableData.setValue(100);
                    } else if (mutableData.getValue(Integer.class)!=0){
                        int health = mutableData.getValue(Integer.class);
                        health -= value;
                        if (health > 0) {
                            mutableData.setValue(health);
                        } else {
                            //they're dead, inc my score by 1
                            incrementScore(1, myReference);
                            mutableData.setValue(0);
                        }
                    }

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Successfully decremented health for player " + reference.getKey());
                }
            });
        }

        return !hitYourself;
    }
}
