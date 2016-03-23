package com.taserlag.lasertag.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.fragments.CreateGameFragment;
import com.taserlag.lasertag.fragments.JoinGameFragment;
import com.taserlag.lasertag.fragments.MainMenuFragment;
import com.taserlag.lasertag.fragments.StatsFragment;
import com.taserlag.lasertag.player.Player;

public class MenuActivity extends AppCompatActivity {

    private final String TAG = "MenuActivity";

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    public static int MenuFragId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Player.getInstance();

        setContentView(R.layout.activity_menu);

        if (getSupportFragmentManager().findFragmentById(R.id.menu_frame) == null) {
            MainMenuFragment mf = new MainMenuFragment();
            MenuFragId = addFragment(R.id.menu_frame, mf, "main_menu_fragment");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check camera and location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission was granted
                } else {
                    // Camera permission was denied
                    // Exit the application
                    System.exit(0);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission was granted
                } else {
                    // Location permission was denied
                    // Exit the application
                    System.exit(0);
                }
                return;
            }
        }
    }

    public int addFragment(int containerViewID, Fragment fragment, String tag) {
        return getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewID, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    public void replaceFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewID, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    public Fragment findFragmentByTag(String tag){
        return getSupportFragmentManager()
                .findFragmentByTag(tag);
    }

    // main menu to create game fragment
    public void showCreateGame(View view){
        CreateGameFragment fragment = new CreateGameFragment();
        replaceFragment(R.id.menu_frame, fragment, "create_game_fragment");
    }

    // reads input on create game screen and saves game to database
    // saveGame changes fragment on success
    public void showGameLobby(View view){
        CreateGameFragment cgf = (CreateGameFragment) getSupportFragmentManager().findFragmentByTag("create_game_fragment");
        cgf.saveGame();
    }

    //Passes game reference to FPSActivity
    public void launchFPS() {
        startActivity(new Intent(this, FPSActivity.class));
        finish();
    }

    // main menu to join game fragment
    public void showJoinGame(View view){
        JoinGameFragment fragment = new JoinGameFragment();
        replaceFragment(R.id.menu_frame, fragment, "join_game_fragment");
    }

    // main menu to stats fragment
    public void showStats(View view) {
        StatsFragment fragment = new StatsFragment();
        replaceFragment(R.id.menu_frame, fragment, "stats_fragment");
    }

    public void logOut(View view) {
        Player.disconnect();
        LaserTagApplication.firebaseReference.unauth();
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        if (getActiveFragment().equals("main_menu_fragment")){
            System.exit(1);
        } else {
            super.onBackPressed();
        }
    }

    public String getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        return getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
    }
}
