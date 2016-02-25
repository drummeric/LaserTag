package com.taserlag.lasertag.application;

import android.app.Application;
import com.firebase.client.Firebase;

public class LaserTagApplication extends Application {
    public static Firebase firebaseReference;
    private final static String TAG = "LaserTagApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        firebaseReference = new Firebase("https://brilliant-inferno-4012.firebaseio.com/");
    }
}