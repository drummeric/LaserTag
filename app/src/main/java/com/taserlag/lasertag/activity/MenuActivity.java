package com.taserlag.lasertag.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.parse.ParseUser;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.fragments.CreateGameFragment;
import com.taserlag.lasertag.fragments.GameLobbyFragment;
import com.taserlag.lasertag.fragments.MainMenuFragment;

public class MenuActivity extends AppCompatActivity implements MainMenuFragment.OnFragmentInteractionListener, CreateGameFragment.OnFragmentInteractionListener, GameLobbyFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (getSupportFragmentManager().findFragmentById(R.id.menu_frame) == null) {
            MainMenuFragment mf = MainMenuFragment.newInstance("", "");
            addFragment(R.id.menu_frame, mf, "main_menu_fragment");
        }
    }

    protected void addFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewID, fragment, tag)
                .commit();
    }

    protected void attachFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .attach(fragment)
                .commit();
    }

    protected void replaceFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewID, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    protected void detachFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .detach(fragment)
                .addToBackStack(null)
                .commit();
    }

    public void onFragmentInteraction(Uri uri){
        //empty
    }

    public void showCreateGame(View view){
        CreateGameFragment fragment = new CreateGameFragment();
        replaceFragment(R.id.menu_frame, fragment, "create_game_fragment");
    }

    public void showGameLobby(View view){
        CreateGameFragment cgf = (CreateGameFragment) getSupportFragmentManager().findFragmentByTag("create_game_fragment");
        cgf.saveGame();

        GameLobbyFragment fragment = new GameLobbyFragment();
        replaceFragment(R.id.menu_frame, fragment, "game_lobby_fragment");
    }

    private Fragment findFragmentByTag(String tag){
        return getSupportFragmentManager()
                .findFragmentByTag(tag);
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
