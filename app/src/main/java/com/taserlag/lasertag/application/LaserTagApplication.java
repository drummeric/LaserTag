package com.taserlag.lasertag.application;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseObject;
import com.taserlag.lasertag.game.FFAGame;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.TeamDeathmatchGame;
import com.taserlag.lasertag.game.VIPGame;

public class LaserTagApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Game.class);
        ParseObject.registerSubclass(FFAGame.class);
        ParseObject.registerSubclass(TeamDeathmatchGame.class);
        ParseObject.registerSubclass(VIPGame.class);
        Parse.initialize(this);
    }
}