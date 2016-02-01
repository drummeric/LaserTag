package com.taserlag.lasertag.application;

import android.app.Application;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.taserlag.lasertag.game.Game;

public class LaserTagApplication extends Application {

    public static Client kinveyClient;
    private final String TAG = "LaserTagApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        kinveyClient = new Client.Builder(this.getApplicationContext()).build();
        kinveyClient.ping(new KinveyPingCallback() {
            public void onFailure(Throwable t) {
                Log.e(TAG, "Kinvey Ping Failed", t);
            }

            public void onSuccess(Boolean b) {
                Log.d(TAG, "Kinvey Ping Success");
            }
        });

    }

}