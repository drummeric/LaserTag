package com.taserlag.lasertag.application;

import android.app.Application;
import android.util.Log;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.taserlag.lasertag.player.Player;

public class LaserTagApplication extends Application {

    public static Client kinveyClient;
    private final static String TAG = "LaserTagApplication";
    public static Player globalPlayer;

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

    public static void setGlobalPlayer(){
        AsyncAppData<Player> myPlayer = LaserTagApplication.kinveyClient.appData("players", Player.class);
        String str = (String)(kinveyClient.user().get("playerReference"));

        myPlayer.getEntity(str, new KinveyClientCallback<Player>() {
            @Override
            public void onSuccess(Player result) {
                Log.v(TAG, "received " + result.getId());
                globalPlayer = result;
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "failed to fetchByFilterCriteria", error);
            }
        });
    }

}