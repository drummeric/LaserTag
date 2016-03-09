package com.taserlag.lasertag.game;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBGame{

    private String host;

    private GameType gameType;

    private boolean scoreEnabled;

    private int score;

    private boolean timeEnabled;

    private int minutes;

    private int maxTeamSize;

    private boolean privateMatch;

    private boolean friendlyFire;

    private boolean gameReady = false;

    private boolean gameOver = false;

    private Map<String, List<String>> fullKeys = new HashMap<>();

    public DBGame() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public boolean getScoreEnabled() {
        return scoreEnabled;
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        this.scoreEnabled = scoreEnabled;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean getTimeEnabled() {
        return timeEnabled;
    }

    public void setTimeEnabled(boolean timeEnabled) {
        this.timeEnabled = timeEnabled;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int size) {
        this.maxTeamSize = size;
    }

    public boolean getPrivateMatch() {
        return privateMatch;
    }

    public void setPrivateMatch(boolean privateMatch) {
        this.privateMatch = privateMatch;
    }

    public boolean getFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public boolean isGameReady() {
        return gameReady;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Map<String, List<String>> getFullKeys() {
        return fullKeys;
    }

    public void setFullKeys(Map<String, List<String>> fullKeys) { this.fullKeys = fullKeys; }

    public static void endGame(Firebase game){
        game.child("gameOver").setValue(true);
    }

    public static void enableTeamListeners(Firebase reference){

        reference.child("fullKeys").addChildEventListener(new ChildEventListener() {
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

    //teamFullKey ends in ":~" for new team
    public static boolean addGlobalPlayer(final String teamFullKey, final Firebase gameReference){
        gameReference.runTransaction(new Transaction.Handler() {
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
                    Player.getInstance().saveActiveGameKey(gameReference.getKey());
                }
            }
        });

        return true;
    }

    public static boolean removeGlobalPlayer(final String teamFullKey, Firebase gameReference) {
        gameReference.runTransaction(new Transaction.Handler() {
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
                    Player.getInstance().resetReady();
                }
            }
        });
        return true;
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
}
