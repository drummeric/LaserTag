package com.taserlag.lasertag.game;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Game {

    private static final String TAG = "Game";

    private static Game instance = null;
    private static DBGame mDBGame;
    private static Firebase mDBGameReference;
    private static ValueEventListener mGameListener;
    private static List<GameFollower> followers = new ArrayList<>();

    private Game() {

    }

    // Called to initialize a new game on the game reference.
    public static Game getInstance(DBGame dbGame, Firebase gameReference) {
        instance = new Game();
        mDBGame = dbGame;
        mDBGameReference = gameReference;
        mGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //crashes here when rebuilding when you were in a game already
                // real users are not expected to crash here, so we won't worry about it
                Log.i(TAG, "Game has been successfully updated for game: " + ((mDBGameReference != null) ? mDBGameReference.getKey() : "null"));
                DBGame newDBGame = dataSnapshot.getValue(DBGame.class);
                if (mDBGame!=null&&newDBGame!=null){

                    if (!mDBGame.isGameLoaded()&&newDBGame.isGameLoaded()){
                        notifyFollowersLoaded();
                    }

                    if (!mDBGame.isGameReady()&&newDBGame.isGameReady()){
                        notifyFollowersReady();
                    }

                    if (!mDBGame.isGameOver()&&newDBGame.isGameOver()){
                        notifyFollowersOver();
                    }

                    if (mDBGame.getTotalScore()!=newDBGame.getTotalScore()){
                        notifyFollowersScoreUpdated();
                    }

                } else if (newDBGame==null && !Player.getInstance().getName().equals(mDBGame.getHost())) {
                    notifyFollowersDeleted();
                }

                mDBGame = newDBGame;
                notifyFollowers();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Game failed to update for game: " + mDBGameReference.getKey(), firebaseError.toException());
            }
        };

        mDBGameReference.addValueEventListener(mGameListener);
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

    public GameType getGameType() {
        return mDBGame.getGameType();
    }

    public boolean getScoreEnabled() {
        return mDBGame.getScoreEnabled();
    }

    public int getEndScore() {
        return mDBGame.getEndScore();
    }

    public boolean getTimeEnabled() {
        return mDBGame.getTimeEnabled();
    }

    public int getEndMinutes() {
        return mDBGame.getEndMinutes();
    }

    public int getMaxTeamSize() {
        return mDBGame.getMaxTeamSize();
    }

    public boolean getFriendlyFire() {
        return mDBGame.getFriendlyFire();
    }

    public boolean isGameReady() {
        return mDBGame.isGameReady();
    }

    public boolean isLoaded() {
        return mDBGame.isGameLoaded();
    }

    public void checkLoaded(){
        mDBGame.checkLoaded(mDBGameReference);
    }

    public boolean isGameOver() {
        return mDBGame.isGameOver();
    }

    public Map<String, DBTeam> getTeams() {
        return mDBGame.getTeams();
    }

    public void checkReady() {
        mDBGame.checkReady(mDBGameReference);
    }

    public void resetReady() {
        mDBGame.resetReady(mDBGameReference);
    }

    public void saveGameOver(){
        mDBGame.saveGameOver(mDBGameReference);
    }

    public void incrementTotalScore(int value){
        mDBGame.incrementTotalScore(value, mDBGameReference);
    }

    // finds player in game (call as little as possible)
    // returns the team of player found
    public DBTeam findPlayer(String name){
        return mDBGame.findPlayer(name);
    }

    //if the team name doesn't exist, create the team in DB and put me on it
    public boolean createTeamWithPlayer(DBTeam team){
        return !teamNameExists(team) && mDBGame.createTeamWithPlayer(team, mDBGameReference);
    }

    private boolean teamNameExists(DBTeam team){
        for(String name : mDBGame.getTeams().keySet()){
            if (team.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public void endGame(){
        mDBGameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DBGame dbGame = dataSnapshot.getValue(DBGame.class);
                LaserTagApplication.firebaseReference.child("finishedGames").child(getKey()).setValue(dbGame);
                deleteGame();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void deleteGame() {
        LaserTagApplication.firebaseReference.child("gameLocations").child(mDBGameReference.getKey()).setValue(null);
        mDBGameReference.setValue(null);
        mDBGameReference.removeEventListener(mGameListener);
        mDBGame = null;
        mDBGameReference = null;
    }

    //called when you enter join game or create game screens
    // and game lobby when you get kicked out of the lobby
    //Resets Team and Player
    public void leaveGame(){
        mDBGame = null;
        if (mDBGameReference!=null){
            mDBGameReference.removeEventListener(mGameListener);
        }
        mDBGameReference = null;
        Team.getInstance().leaveTeam();
    }

    private static void notifyFollowers() {
        for (GameFollower follower : followers) {
            follower.notifyGameUpdated();
        }
    }

    private static void notifyFollowersLoaded() {
        for (GameFollower follower : followers) {
            follower.notifyGameLoaded();
        }
    }

    private static void notifyFollowersReady() {
        for (GameFollower follower : followers) {
            follower.notifyGameReady();
        }
    }

    private static void notifyFollowersOver() {
        for (GameFollower follower : followers) {
            follower.notifyGameOver();
        }
    }

    private static void notifyFollowersScoreUpdated() {
        for (GameFollower follower : followers) {
            follower.notifyGameScoreUpdated();
        }
    }

    private static void notifyFollowersDeleted() {
        for (GameFollower follower : followers) {
            follower.notifyGameDeleted();
        }
    }

    public void registerForUpdates(GameFollower follower) {
        if (!followers.contains(follower)) {
            followers.add(follower);
            if (mDBGameReference!=null) {
                mDBGameReference.removeEventListener(mGameListener);
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

    public List<DBTeam> getSortedTeams(){
        List<DBTeam> teamList = new ArrayList<>(mDBGame.getTeams().values());
        Collections.sort(teamList);
        return teamList;
    }

    public void initGameCaptains(){
        for (DBTeam dbTeam : getTeams().values()){
            dbTeam.setTeamCaptain();
        }
    }

    @Override
    public String toString(){
        return mDBGame.toString();
    }

    public TeamIterator<DBPlayer> makeIterator() {
        return mDBGame.makeIterator();
    }
}
