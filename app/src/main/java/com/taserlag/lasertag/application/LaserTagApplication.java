package com.taserlag.lasertag.application;

import android.app.Application;
import android.content.Context;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;

public class LaserTagApplication extends Application {
    public static Firebase firebaseReference;
    public static GeoFire geoFire;

    private final static String TAG = "LaserTagApplication";
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        firebaseReference = new Firebase("https://brilliant-inferno-4012.firebaseio.com/");
        geoFire = new GeoFire(firebaseReference.child("gameLocations"));

    }

    public static Context getAppContext() {
        return LaserTagApplication.context;
    }

    public static String getUid(){
        return LaserTagApplication.firebaseReference.getAuth().getUid();
    }
}