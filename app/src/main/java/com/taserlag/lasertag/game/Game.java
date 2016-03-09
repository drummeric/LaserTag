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
        enableListeners();
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
        mDBGameReference.child("gameOver").setValue(true);
    }

    public static void enableListeners() {

        mDBGameReference.child("fullKeys").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            //player added
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                LaserTagApplication.firebaseReference.child("teams").child(dataSnapshot.getKey().split(":~")[1]).setValue(null);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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

        mDBGameReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue(DBGame.class) == null) {
                    //should never happen
                    currentData.setValue(new DBGame());
                } else {
                    DBGame dbGame = currentData.getValue(DBGame.class);

                    //if the team isn't in the game's map
                    if (dbGame.getFullKeys().get(teamFullKey) == null) {
                        Team team = new Team(teamFullKey.split(":~")[0]);

                        // add global player to new team
                        List<String> playerKey = new ArrayList<String>();
                        playerKey.add(LaserTagApplication.firebaseReference.getAuth().getUid());

                        // if this is a new team
                        if (teamFullKey.endsWith(":~")) {
                            Firebase ref = LaserTagApplication.firebaseReference.child("teams").push();
                            ref.setValue(team);
                            dbGame.getFullKeys().put(teamFullKey + ref.getKey(), playerKey);
                        } else {
                            //existing team not on game map (due to database propogation)
                            LaserTagApplication.firebaseReference.child("teams").child(teamFullKey).setValue(team);
                            dbGame.getFullKeys().put(teamFullKey, playerKey);
                        }

                    } else {
                        dbGame.getFullKeys().get(teamFullKey).add(LaserTagApplication.firebaseReference.getAuth().getUid());
                    }
                    currentData.setValue(dbGame);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    LaserTagApplication.firebaseReference
                            .child("users")
                            .child(LaserTagApplication.firebaseReference.getAuth().getUid())
                            .child("player")
                            .child("activeGameKey")
                            .setValue(mDBGameReference.getKey());
                    restoreGameMap(currentData.getValue(DBGame.class));
                }
            }
        });

        return true;
    }

    public boolean removeGlobalPlayer(final String teamFullKey) {

        mDBGameReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue(DBGame.class) == null) {
                    currentData.setValue(new DBGame());
                } else {
                    DBGame dbGame = currentData.getValue(DBGame.class);
                    dbGame.getFullKeys().get(teamFullKey).remove(LaserTagApplication.firebaseReference.getAuth().getUid());

                    //empty team -> remove team from database
                    if (dbGame.getFullKeys().get(teamFullKey).isEmpty()) {
                        dbGame.getFullKeys().remove(teamFullKey);
                    }

                    currentData.setValue(dbGame);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    Player.getInstance().resetActiveGameKey();
                    LaserTagApplication.firebaseReference
                            .child("users")
                            .child(LaserTagApplication.firebaseReference.getAuth().getUid())
                            .child("player")
                            .child("ready")
                            .setValue(false);
                    restoreGameMap(currentData.getValue(DBGame.class));
                }
            }
        });
        return true;
    }

    public boolean removeGlobalPlayer() {
        return removeGlobalPlayer(findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid()));
    }

    public void restoreGameMap(DBGame dbGame){
        mDBGame.setFullKeys(dbGame.getFullKeys());
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

    @Override
    public String toString(){
        StringBuilder description = new StringBuilder();

        description.append(getHost()).append("'s ");

        if (getPrivateMatch()) {
            description.append("private ");
        }

        description.append(getGameType()).append(" game ");

        if (getScoreEnabled()) {
            description.append("to ").append(getScore()).append(" points");
        }

        if (getTimeEnabled()) {
            if(getScoreEnabled()) {
                description.append(" or ");
            }
            description.append("until ").append(getMinutes()).append(" minutes have elapsed");

        }

        description.append(". Friendly fire is ");

        if (getFriendlyFire()) {
            description.append("enabled.");
        } else {
            description.append("disabled.");
        }

        if (!getGameType().equals(GameType.FFA)){
            description.append(" The maximum team size is ").append(getMaxTeamSize()).append(".");
        }

        return description.toString();
    }

    public void startGameListeners() {
        mDBGameReference.child("gameOver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot gameOverSnapshot) {
                Boolean gameOver = gameOverSnapshot.getValue(Boolean.class);
                if (gameOver!=null && gameOver){
                    FPSActivity.gameOver();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void deleteGame() {
        mDBGameReference.setValue(null);
        mDBGameReference.removeEventListener(mGameListener);
        mDBGame = null;
        mDBGameReference = null;
    }

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

            if (mDBGame != null) {
                notifyFollowers();
            }
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

}
