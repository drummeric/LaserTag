package com.taserlag.lasertag.team;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.application.LaserTagApplication;

public class Team{

    private final static String TAG = "Team";

    private String name = "Team Name";

    private int score = 0;

    public Team() {}

    public Team(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore(){
        return score;
    }

    public static void incScore(final int value, final String teamUID){
        LaserTagApplication.firebaseReference.child("teams").child(teamUID).child("score").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue(Integer.class) == null) {
                    mutableData.setValue(0);
                } else {
                    mutableData.setValue(mutableData.getValue(Integer.class) + value);
                }
                if (FPSActivity.getGame().getScoreEnabled()&&FPSActivity.getGame().getScore()<=mutableData.getValue(Integer.class)){
                    FPSActivity.getGame().endGame(FPSActivity.getGameReference());
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.i(TAG, "Successfully incremented score for teamUID" + teamUID);
            }
        });
    }

    @Override
    public boolean equals(Object team){
        return (team instanceof Team) && (((Team) team).getName().equals(name));
    }

    public void setCaptain(){
       //todo write this function
    }
}
