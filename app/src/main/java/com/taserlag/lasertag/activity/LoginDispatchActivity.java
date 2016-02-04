package com.taserlag.lasertag.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.taserlag.lasertag.application.LaserTagApplication;

public class LoginDispatchActivity extends AppCompatActivity {

    private final String TAG = "LoginDispatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Checks if a user is logged in
        if (LaserTagApplication.kinveyClient.user().isUserLoggedIn()) {

            LaserTagApplication.kinveyClient.user().retrieve(new KinveyUserCallback() {
                @Override
                public void onSuccess(User user) {
                    Log.v(TAG, "Retrieved user " + user.getId());
                    LaserTagApplication.setGlobalPlayer();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(TAG, "Failed to load user" , throwable);
                }
            });


            // Starts MenuActivity if logged in
            Intent i = new Intent(LoginDispatchActivity.this, MenuActivity.class);
            finish();
            startActivity(i);
        } else {
            // Starts LoginActivity if not logged in
            Intent i = new Intent(LoginDispatchActivity.this, LoginActivity.class);
            finish();
            startActivity(i);
        }
        super.onCreate(savedInstanceState);
    }
}
