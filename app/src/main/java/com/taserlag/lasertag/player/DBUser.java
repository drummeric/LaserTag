package com.taserlag.lasertag.player;


import com.taserlag.lasertag.application.LaserTagApplication;

import java.util.ArrayList;
import java.util.List;
// stores player stuff in DB for non game specific things
public class DBUser {

    private static final String TAG = "DBUser";

    private String username;

    private float[] color = new float[3];

    private List<String> previousGames = new ArrayList<>();

    public DBUser(){
    }

    public DBUser(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public float[] getColor() {
        return color;
    }

    public void saveColor(float[] c) {
        color = c;
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.getUid()).child("color").setValue(color);
    }

    public void setColor(float[] c){
        color = c;
    }

    public List<String> getPreviousGames() {
        return previousGames;
    }

    public void archiveGame(String gameKey){
        previousGames.add(gameKey);
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.getUid()).child("previousGames").setValue(previousGames);
    }
}
