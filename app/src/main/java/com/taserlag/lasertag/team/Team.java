package com.taserlag.lasertag.team;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameType;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Team{

    private final static String TAG = "Team";
    
    private static ValueEventListener mTeamListener;
    private static Firebase mDBTeamReference;
    private static DBTeam mDBTeam;
    private static Team instance;
    private static List<TeamFollower> followers = new ArrayList<>();

    private Team() {
    }

    public static Team getInstance(DBTeam dbTeam) {
        instance = new Team();
        mDBTeam = dbTeam;
        mDBTeamReference = Game.getInstance().getReference().child("teams").child(mDBTeam.getName());
        mTeamListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mDBTeamReference!=null) {
                    Log.i(TAG, "Team has been successfully updated for team: " + mDBTeamReference.getKey());
                    DBTeam newTeam = dataSnapshot.getValue(DBTeam.class);

                    if (newTeam!=null && mDBTeam !=null && newTeam.isCaptainDead()&&!mDBTeam.isCaptainDead()){
                        notifyFollowersCaptainDead();
                    }

                    mDBTeam = newTeam;
                    notifyFollowers();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                if (mDBTeamReference!=null) {
                    Log.e(TAG, "Team failed to update for team: " + mDBTeamReference.getKey(), firebaseError.toException());
                }
            }
        };

        mDBTeamReference.addValueEventListener(mTeamListener);
        return instance;
    }

    public static Team getInstance() {
        if (instance==null) {
            instance = new Team();
        }
        return instance;
    }

    public DBTeam getDBTeam(){
        return mDBTeam;
    }

    public String getName() {
        return mDBTeam.getName();
    }

    public int getScore(){
        return mDBTeam.getScore();
    }

    public void checkReady(){
        mDBTeam.checkReady(mDBTeamReference);
    }

    public void resetReady(){
        mDBTeam.resetReady(mDBTeamReference);
    }

    public void checkLoaded() {
        mDBTeam.checkLoaded(mDBTeamReference);
    }

    public void saveCaptainDead(boolean captainDead){
        mDBTeam.saveCaptainDead(captainDead, mDBTeamReference);
    }

    public boolean isCaptainDead(){
        return mDBTeam.isCaptainDead();
    }

    public void resetTeamCaptain(){
        mDBTeam.resetTeamCaptain();
    }

    public Firebase getDBTeamReference(){
        return mDBTeamReference;
    }

    public Map<String, DBPlayer> getPlayers() {
        return mDBTeam.getPlayers();
    }

    //called when you enter join Team or create Team screens
    // or when you leave a team in game lobby
    // Also resets Player's dbPlayer
    public void leaveTeam() {
        mDBTeam = null;
        if (mDBTeamReference!=null) {
            mDBTeamReference.removeEventListener(mTeamListener);
            mDBTeamReference = null;
        }
        Player.getInstance().leave();
    }

    public boolean removeDBPlayer() {
        return mDBTeam!=null && mDBTeam.removeDBPlayer(mDBTeamReference);
    }

    public void incrementScore(final int value) {
        mDBTeam.incrementScore(value, mDBTeamReference);
    }

    private static void notifyFollowers() {
        for (TeamFollower follower : followers) {
            follower.notifyTeamUpdated();
        }
    }

    private static void notifyFollowersCaptainDead() {
        for (TeamFollower follower : followers) {
            follower.notifyTeamCaptainDied();
        }
    }

    public void registerForUpdates(TeamFollower follower) {
        if (!followers.contains(follower)) {
            followers.add(follower);

            if (mDBTeam != null) {
                notifyFollowers();
            }
        }
    }

    public void unregisterForUpdates(TeamFollower follower) {
        followers.remove(follower);
    }

    public Iterator<DBPlayer> makeIterator(){
        return mDBTeam.makeIterator();
    }
}
