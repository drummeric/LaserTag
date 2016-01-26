package com.taserlag.lasertag.application;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseObject;
import com.taserlag.lasertag.game.Game;

public class LaserTagApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Game.class);
        Parse.initialize(this);
    }
}