package com.taserlag.lasertag.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.taserlag.lasertag.application.LaserTagApplication;

public class LoginDispatchActivity extends AppCompatActivity {

    private final String TAG = "LoginDispatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (LaserTagApplication.firebaseReference.getAuth()!=null && LaserTagApplication.firebaseReference.getAuth().getUid()!=null){
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

    }
}
