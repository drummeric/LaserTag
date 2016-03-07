package com.taserlag.lasertag.player;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.weapon.FastWeapon;
import com.taserlag.lasertag.weapon.StrongWeapon;
import com.taserlag.lasertag.weapon.Weapon;

public class Player{

    private static final String TAG = "player";

    //health in DB is actually realHealth + shieldStrength
    private int realHealth = 100;

    private boolean mPrimaryWeaponActive = true;

    private Weapon mPrimaryWeapon = new FastWeapon();
    private Weapon mSecondaryWeapon = new StrongWeapon();
    private Shield mShield = new Shield();
    public static DBPlayer dbPlayer;
    private static ValueEventListener playerListener;

    private static Player instance = null;

    public static Player getInstance(){
        if (instance==null){
            instance = new Player();
            playerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (LaserTagApplication.firebaseReference.getAuth().getUid() != null) {
                        Log.i(TAG, "Player has been successfully updated for user: " + LaserTagApplication.firebaseReference.getAuth().getUid());
                        dbPlayer = dataSnapshot.getValue(DBPlayer.class);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, "Player failed to updated for user: " + LaserTagApplication.firebaseReference.getAuth().getUid(), firebaseError.toException());
                }
            };

            LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").addValueEventListener(playerListener);
        }
        return instance;
    }

    public static void reset(){
        //reinits shields and weapons to new objects
        instance = new Player();
    }

    public boolean isPrimaryWeaponActive() {
        return mPrimaryWeaponActive;
    }

    public Weapon retrieveActiveWeapon() {
        if (mPrimaryWeaponActive) {
            return mPrimaryWeapon;
        } else {
            return mSecondaryWeapon;
        }
    }

    public boolean deployShield(){
        boolean deployable = mShield.deploy();
        if (deployable) {
            dbPlayer.incrementHealth(mShield.getStrength());
        }
        return deployable;
    }

    public Shield getShield() {
        return mShield;
    }

    public void swapWeapon() {
        mPrimaryWeaponActive = !mPrimaryWeaponActive;
    }

    // decrement realHealth by the damage not absorbed by the shield
    public void decrementHealth(int value){
        int realHealthDamage = mShield.decStrength(realHealth+mShield.getStrength()-value);
        if (realHealthDamage!=0){
            realHealth -= realHealthDamage;
            FPSActivity.updateHealthText();
        }
    }

    public int getRealHealth(){
        return realHealth;
    }

    public int getTotalHealth(){
        return realHealth + mShield.getStrength();
    }


    public static void disconnect(){
        instance = null;
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").removeEventListener(playerListener);
    }

    public String getName() {
        return dbPlayer.getName();
    }

    public void setName(String name) {
        dbPlayer.setName(name);
    }

    public int getScore() {
        return dbPlayer.getScore();
    }

    public void setScore(int score) {
        dbPlayer.setScore(score);
    }

    public int getHealth() {
        return dbPlayer.getHealth();
    }

    public void setHealth(int health) {
        dbPlayer.setHealth(health);
    }

    public boolean isCaptain() {
        return dbPlayer.isCaptain();
    }

    public void setCaptain(boolean captain) {
        dbPlayer.setCaptain(captain);
    }

    public int[] getColor() {
        return dbPlayer.getColor();
    }

    public void setColor(int[] color) {
        dbPlayer.setColor(color);
    }

    public String getActiveGameKey() {
        return dbPlayer.getActiveGameKey();
    }

    public void setActiveGameKey(String activeGameKey) {
        dbPlayer.setActiveGameKey(activeGameKey);
    }

    public boolean isReady() {
        return dbPlayer.isReady();
    }

    public void setReady(boolean ready) {
        dbPlayer.setReady(ready);
    }

    public void resetActiveGameKey(){
        dbPlayer.resetActiveGameKey();
    }

    // can only reset my health, does not take playerUID
    public void resetHealthAndScore(){
        dbPlayer.resetHealthAndScore();
    }

    // decrement other people's health, cannot hurt yourself
    // returns false if you try to decrement your own health
    public boolean decrementHealthAndIncMyScore(final int value,final String playerUID, final String teamUID){
        return dbPlayer.decrementHealthAndIncMyScore(value, playerUID, teamUID);
    }

}
