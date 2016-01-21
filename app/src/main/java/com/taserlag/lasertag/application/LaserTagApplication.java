package com.taserlag.lasertag.application;

import android.app.Application;
import com.parse.Parse;

public class LaserTagApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this);
    }
}