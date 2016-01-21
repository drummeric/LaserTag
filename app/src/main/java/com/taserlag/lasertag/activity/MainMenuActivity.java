package com.taserlag.lasertag.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.parse.ParseUser;
import com.taserlag.lasertag.R;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void launchFPS(View view) {
        Intent intent = new Intent(this, FPSActivity.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        ParseUser.logOut();
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
