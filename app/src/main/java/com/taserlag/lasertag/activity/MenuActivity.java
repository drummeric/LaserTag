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

public class MenuActivity extends AppCompatActivity implements MainMenuFragment.OnFragmentInteractionListener, CreateGameFragment.OnFragmentInteractionListener, GameLobbyFragment.OnFragmentInteractionListener, JoinGameFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    // shows createTeamDialog in game lobby
    public void showCreateTeam(View view){
        GameLobbyFragment glf = (GameLobbyFragment) getSupportFragmentManager().findFragmentByTag("game_lobby_fragment");
        glf.doCreateTeam();
    }

    public void launchFPS(View view) {
        Intent intent = new Intent(this, FPSActivity.class);
        startActivity(intent);
    }

    // main menu to join game fragment
    public void showJoinGame(View view){
        JoinGameFragment fragment = new JoinGameFragment();
        replaceFragment(R.id.menu_frame, fragment, "join_game_fragment");

    }


    public void logOut(View view) {
        LaserTagApplication.kinveyClient.user().logout().execute();
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
