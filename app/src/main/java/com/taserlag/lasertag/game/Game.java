package com.taserlag.lasertag.game;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    private static final String TAG = "Game";

    private static Game instance = null;
    private static DBGame mDBGame;
    private static Firebase mDBGameReference;
    private static ValueEventListener mGameListener;
    private static List<GameFollower> followers = new ArrayList<GameFollower>();

    private Game() {

    }

    public static Game getInstance(Firebase gameReference) {
        instance = new Game();
        mDBGameReference = gameReference;
        mGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "Game has been successfully updated for game: " + mDBGameReference.getKey());
                mDBGame = dataSnapshot.getValue(DBGame.class);
                notifyFollowers();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Game failed to update for game: " + mDBGameReference.getKey(), firebaseError.toException());
            }
        };

        mDBGameReference.addValueEventListener(mGameListener);
        mDBGame.enableTeamListeners(mDBGameReference);
        return instance;
    }

    public static Game getInstance() {
        if (instance==null) {
            instance = new Game();
        }
        return instance;
    }

    public String getKey() {
        return mDBGameReference.getKey();
    }

    public Firebase getReference() {
        return mDBGameReference;
    }

    public DBGame getDBGame() {
        return mDBGame;
    }

    public String getHost() {
        return mDBGame.getHost();
    }

    public void setHost(String host) {
        mDBGame.setHost(host);
    }

    public GameType getGameType() {
        return mDBGame.getGameType();
    }

    public void setGameType(GameType gameType) {
        mDBGame.setGameType(gameType);
    }

    public boolean getScoreEnabled() {
        return mDBGame.getScoreEnabled();
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        mDBGame.setScoreEnabled(scoreEnabled);
    }

    public int getScore() {
        return mDBGame.getScore();
    }

    public void setScore(int score) {
        mDBGame.setScore(score);
    }

    public boolean getTimeEnabled() {
        return mDBGame.getTimeEnabled();
    }

    public void setTimeEnabled(boolean timeEnabled) {
        mDBGame.setTimeEnabled(timeEnabled);
    }

    public int getMinutes() {
        return mDBGame.getMinutes();
    }

    public void setMinutes(int minutes) {
        mDBGame.setMinutes(minutes);
    }

    public int getMaxTeamSize() {
        return mDBGame.getMaxTeamSize();
    }

    public void setMaxTeamSize(int size) {
        mDBGame.setMaxTeamSize(size);
    }

    public boolean getPrivateMatch() {
        return mDBGame.getPrivateMatch();
    }

    public void setPrivateMatch(boolean privateMatch) {
        mDBGame.setPrivateMatch(privateMatch);
    }

    public boolean getFriendlyFire() {
        return mDBGame.getFriendlyFire();
    }

    public void setFriendlyFire(boolean friendlyFire) {
        mDBGame.setFriendlyFire(friendlyFire);
    }

    public boolean isGameReady() {
        return mDBGame.isGameReady();
    }

    public boolean isGameOver() {
        return mDBGame.isGameOver();
    }

    public Map<String, List<String>> getFullKeys() {
        return mDBGame.getFullKeys();
    }

    public void endGame(){
        mDBGame.endGame(mDBGameReference);
    }

    public boolean createTeamWithGlobalPlayer(final Team team){
        if (teamNameExists(team)){
            return false;
        }
        addGlobalPlayer(team.getName() + ":~");

        return true;
    }

    //teamFullKey ends in ":~" for new team
    public boolean addGlobalPlayer(final String teamFullKey){
        String currentTeamFullKey = findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid());

        if (!currentTeamFullKey.equals("")){
            // player is on a team
            removeGlobalPlayer(currentTeamFullKey);
        } else if (currentTeamFullKey.equals(teamFullKey)) {
            //global player on team already, don't need to re-add
            return true;
        } else if (!teamFullKey.endsWith(":~") && mDBGame.getFullKeys().get(teamFullKey).size() >= mDBGame.getMaxTeamSize()){
            //team full
            return false;
        }

        return mDBGame.addGlobalPlayer(teamFullKey, mDBGameReference);
    }

    //faster than the no-param version because you dont have to call findPlayer()
    //use this version when you can (aka when you know the team's fullkey that you're on)
    public boolean removeGlobalPlayer(final String teamFullKey) {
        return mDBGame.removeGlobalPlayer(teamFullKey, mDBGameReference);
    }

    public boolean removeGlobalPlayer() {
        return removeGlobalPlayer(findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid()));
    }

    private boolean teamNameExists(Team team){
        for(String fullKey : mDBGame.getFullKeys().keySet()){
            if (team.getName().equals(fullKey.split(":~")[0])){
                return true;
            }
        }
        return false;
    }

    private String getTeamFullKey(Team team){
        for(String fullKey : mDBGame.getFullKeys().keySet()){
            if (team.getName().equals(fullKey.split(":~")[0])){
                return fullKey;
            }
        }
        return null;
    }

    //Returns teamfullkey of team to which player belongs
    // else ""
    public String findPlayer(String playerUID){
        for (Map.Entry<String, List<String>> entry: mDBGame.getFullKeys().entrySet()){
            for (String playerFoundUID:entry.getValue()){
                if (playerUID.equals(playerFoundUID)){
                    return entry.getKey();
                }
            }
        }
        return "";
    }

    public void deleteGame() {
        mDBGameReference.setValue(null);
        mDBGameReference.removeEventListener(mGameListener);
        mDBGame = null;
        mDBGameReference = null;
    }

    //called when you enter join game or create game screens
    public void resetDBGame() {
        mDBGame = null;
        mDBGameReference = null;
    }

    private static void notifyFollowers() {
        for (GameFollower follower : followers) {
            follower.notifyGameUpdated();
        }
    }

    public void registerForUpdates(GameFollower follower) {
        if (!followers.contains(follower)) {
            followers.add(follower);
            if (mDBGameReference!=null) {
                mDBGameReference.addValueEventListener(mGameListener);
            }
        }

        if (mDBGame != null) {
            notifyFollowers();
        }
    }

    public void unregisterForUpdates(GameFollower follower) {
        followers.remove(follower);
        if (followers.isEmpty()) {
            if (mDBGameReference != null) {
                mDBGameReference.removeEventListener(mGameListener);
            }
        }
    }

    public void setGameReady() {
        mDBGameReference.child("gameReady").setValue(true);
    }

    public void clearGameReady() {
        mDBGameReference.child("gameReady").setValue(false);
    }

    @Override
    public String toString(){
        return mDBGame.toString();
    }
}
