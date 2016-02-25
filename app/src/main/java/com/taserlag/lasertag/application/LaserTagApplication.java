package com.taserlag.lasertag.application;

import android.app.Application;
import android.util.Log;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.player.DBPlayer;

public class LaserTagApplication extends Application {
    public static Firebase firebaseReference;
    private final static String TAG = "LaserTagApplication";
    public static DBPlayer globalPlayer;
    private static ValueEventListener playerListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        firebaseReference = new Firebase("https://brilliant-inferno-4012.firebaseio.com/");
    }

    public static void initGlobalPlayer() {

        playerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (firebaseReference.getAuth().getUid() != null) {
                    Log.i(TAG, "globalPlayer has been successfully updated for user: " + firebaseReference.getAuth().getUid());
                    globalPlayer = dataSnapshot.getValue(DBPlayer.class);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "globalPlayer failed to updated for user: " + firebaseReference.getAuth().getUid(), firebaseError.toException());
            }
        };

        firebaseReference.child("users").child(firebaseReference.getAuth().getUid()).child("player").addValueEventListener(playerListener);
    }

    public static void disconnect(){
        firebaseReference.child("users").child(firebaseReference.getAuth().getUid()).child("player").removeEventListener(playerListener);
    }

}