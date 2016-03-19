package com.taserlag.lasertag.game;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.ArrayList;
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

    public boolean getPrivateMatch() {
        return mDBGame.getPrivateMatch();
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

    public void endGame(){
        mDBGame.endGame(mDBGameReference);
    }

    // finds player in game (call as little as possible)
    // returns the team of player found
    public DBTeam findPlayer(String name){
        for (Map.Entry<String, DBTeam> teamEntry:mDBGame.getTeams().entrySet()){
            for (String playerName:teamEntry.getValue().getPlayers().keySet()){
                if (playerName.equals(name)){
                    return teamEntry.getValue();
                }
            }
        }

        return null;
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

    public void deleteGame() {
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

    @Override
    public String toString(){
        return mDBGame.toString();
    }

    public TeamIterator<DBPlayer> makeIterator() {
        return mDBGame.makeIterator();
    }
}
