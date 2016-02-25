package com.taserlag.lasertag.player;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.weapon.FastWeapon;
import com.taserlag.lasertag.weapon.StrongWeapon;
import com.taserlag.lasertag.weapon.Weapon;
// Only class that should be accessing/storing player data to the database
// Static methods for incrementing score/health
public class DBPlayer{

    private static final String TAG = "DBplayer";

    private String name;

    private int score = 0;

    private int health = 100;

    private boolean captain = false;

    private int[] color = new int[4];

    private String activeGameKey = "";
    private boolean ready = false;

    public DBPlayer() {}

    public DBPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isCaptain() {
        return captain;
    }

    public void setCaptain(boolean captain) {
        this.captain = captain;
    }

    public int[] getColor() {
        return color;
    }

    public void setColor(int[] color) {
        this.color = color;
    }

    public String getActiveGameKey() {
        return activeGameKey;
    }

    public void setActiveGameKey(String activeGameKey) {
        this.activeGameKey = activeGameKey;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    //can only increment my score, does not take in playerUID
    private static void incrementScore(final int value, final String teamUID){
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player/score").runTransaction(new Transaction.Handler() {
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
                Log.i(TAG, "Successfully incremented score for player " + LaserTagApplication.firebaseReference.getAuth().getUid());
            }
        });

        LaserTagApplication.firebaseReference.child("teams").child(teamUID).child("score").runTransaction(new Transaction.Handler() {
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
                Log.i(TAG, "Successfully incremented score for team " + teamUID);
            }
        });
    }

    // can only reset my health, does not take playerUID
    public static void resetHealthAndScore(){
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue(DBPlayer.class)!=null){
                    DBPlayer DBplayer = mutableData.getValue(DBPlayer.class);
                    DBplayer.setScore(0);     //reset score and health
                    DBplayer.setHealth(100);
                    mutableData.setValue(DBplayer);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.i(TAG, "Successfully reset health for player " + LaserTagApplication.firebaseReference.getAuth().getUid());
            }
        });
    }

    // decrement other people's health, cannot hurt yourself
    // returns false if you try to decrement your own health
    public static boolean decrementHealthAndIncMyScore(final int value,final String playerUID, final String teamUID){
        boolean hitYourself = LaserTagApplication.firebaseReference.getAuth().getUid().equals(playerUID);

        if (!hitYourself) {
            LaserTagApplication.firebaseReference.child("users").child(playerUID).child("player/health").runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue(Integer.class) == null) {
                        mutableData.setValue(100);
                    } else {
                        int health = mutableData.getValue(Integer.class);
                        health -= value;
                        if (health > 0) {
                            mutableData.setValue(health);
                        } else {
                            //they're dead, inc my score by 1
                            incrementScore(1, teamUID);
                            mutableData.setValue(0);
                        }
                    }

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Successfully decremented health for player " + playerUID);
                }
            });
        }

        return !hitYourself;
    }

}
