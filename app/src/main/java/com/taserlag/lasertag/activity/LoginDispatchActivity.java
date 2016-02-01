package com.taserlag.lasertag.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.taserlag.lasertag.application.LaserTagApplication;

public class LoginDispatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Checks if a user is logged in
        if (LaserTagApplication.kinveyClient.user().isUserLoggedIn()) {
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
