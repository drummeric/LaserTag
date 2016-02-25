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

public class Player{

    private static final String TAG = "player";

    private String name;

    private int score = 0;

    private int health = 100;

    private boolean captain = false;

    private boolean primaryWeaponActive = true;

    private int[] color = new int[4];

    private String activeGameKey = "";
    private boolean ready = false;

    private Weapon primaryWeapon = new FastWeapon();
    private Weapon secondaryWeapon = new StrongWeapon();
    private Shield shield;

    public Player() {}

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, Weapon primaryWeapon, Weapon secondaryWeapon, Shield shield) {
        this.name = name;
        this.primaryWeapon = primaryWeapon;
        this.secondaryWeapon = secondaryWeapon;
        this.shield = shield;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public boolean isPrimaryWeaponActive() {
        return primaryWeaponActive;
    }

    public boolean isCaptain() {
        return captain;
    }

    public int getHealth() {
        return health;
    }

    public int[] getColor() {
        return color;
    }

    public String getActiveGameKey() {
        return activeGameKey;
    }

    public boolean isReady() {
        return ready;
    }

    public Weapon retrieveActiveWeapon() {
        if (primaryWeaponActive) {
            return primaryWeapon;
        } else {
            return secondaryWeapon;
        }
    }

    public Shield getShield() {
        return shield;
    }

    public void swapWeapon() {
        primaryWeaponActive = !primaryWeaponActive;
    }

    public void setCaptain() {
        captain = true;
    }

    public void resetCaptain() {
        captain = false;
    }

    private void resetScore(){
        score = 0;
    }

    private void resetHealth(){
        health = 100;
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
                if (mutableData.getValue(Player.class)!=null){
                    Player player = mutableData.getValue(Player.class);
                    player.resetScore();
                    player.resetHealth();
                    mutableData.setValue(player);
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
