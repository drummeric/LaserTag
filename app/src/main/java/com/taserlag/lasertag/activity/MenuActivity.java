package com.taserlag.lasertag.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.fragments.CreateGameFragment;
import com.taserlag.lasertag.fragments.GameLobbyFragment;
import com.taserlag.lasertag.fragments.JoinGameFragment;
import com.taserlag.lasertag.fragments.MainMenuFragment;
import com.taserlag.lasertag.fragments.SetPlayerColorFragment;
import com.taserlag.lasertag.player.Player;

public class MenuActivity extends AppCompatActivity implements MainMenuFragment.OnFragmentInteractionListener, CreateGameFragment.OnFragmentInteractionListener, GameLobbyFragment.OnFragmentInteractionListener, JoinGameFragment.OnFragmentInteractionListener, SetPlayerColorFragment.OnFragmentInteractionListener{

    private final String TAG = "MenuActivity";
    private static final String GAME_REF_PARAM = "gameRef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Player.getInstance();

        setContentView(R.layout.activity_menu);

        if (getSupportFragmentManager().findFragmentById(R.id.menu_frame) == null) {
            MainMenuFragment mf = new MainMenuFragment();
            addFragment(R.id.menu_frame, mf, "main_menu_fragment");
        }
    }

    protected void addFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewID, fragment, tag)
                .commit();
    }

    public void replaceFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewID, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    public Fragment findFragmentByTag(String tag){
        return getSupportFragmentManager()
                .findFragmentByTag(tag);
    }

    public void onFragmentInteraction(Uri uri){
        //empty
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
    public void launchFPS(String gameReference) {
        Intent intent = new Intent(this, FPSActivity.class);
        intent.putExtra(GAME_REF_PARAM,gameReference);
        startActivity(intent);
        finish();
    }

    // main menu to join game fragment
    public void showJoinGame(View view){
        JoinGameFragment fragment = new JoinGameFragment();
        replaceFragment(R.id.menu_frame, fragment, "join_game_fragment");

    }

    public void logOut(View view) {
        Player.getInstance().disconnect();
        LaserTagApplication.firebaseReference.unauth();
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
