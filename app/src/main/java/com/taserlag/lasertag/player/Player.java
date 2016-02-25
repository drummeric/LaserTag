package com.taserlag.lasertag.player;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.weapon.FastWeapon;
import com.taserlag.lasertag.weapon.StrongWeapon;
import com.taserlag.lasertag.weapon.Weapon;

public class Player{

    private static final String TAG = "player";

    private boolean mPrimaryWeaponActive = true;

    private Weapon mPrimaryWeapon = new FastWeapon();
    private Weapon mSecondaryWeapon = new StrongWeapon();
    private Shield mShield;

    private static Player instance = null;

    public static Player getInstance(){
        if (instance==null){
            instance = new Player();

        }
        return instance;
    }

    public static void reset(){
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

    public Shield getShield() {
        return mShield;
    }

    public void swapWeapon() {
        mPrimaryWeaponActive = !mPrimaryWeaponActive;
    }

}
