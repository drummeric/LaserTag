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
    private final String TAG = "LaserTag_Kinvey";

    @Override
    public void onCreate() {
        super.onCreate();

        kinveyClient = new Client.Builder(this.getApplicationContext()).build();
        kinveyClient.ping(new KinveyPingCallback() {
            public void onFailure(Throwable t) {
                Log.e("LaserTag_Kinvey", "Kinvey Ping Failed", t);
            }

            public void onSuccess(Boolean b) {
                Log.d("LaserTag_Kinvey", "Kinvey Ping Success");
            }
        });


        kinveyClient.user().login(new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Login Failure", error);
            }

            @Override
            public void onSuccess(User result) {
                Log.i(TAG, "Logged in a new implicit user with id: " + result.getId());
            }
        });

    }


}