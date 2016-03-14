package com.taserlag.lasertag.game;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBGame{

    private String host;

    private GameType gameType;

    private boolean scoreEnabled;

    private int endScore;

    private boolean timeEnabled;

    private int endMinutes;

    private int maxTeamSize;

    private boolean privateMatch;

    private boolean friendlyFire;

    private boolean gameReady = false;

    private boolean gameLoaded = false;

    private boolean gameOver = false;

    private Map<String, DBTeam> teams = new HashMap<>();

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

    public int getEndScore() {
        return endScore;
    }

    public void setEndScore(int score) {
        this.endScore = score;
    }

    public boolean getTimeEnabled() {
        return timeEnabled;
    }

    public void setTimeEnabled(boolean timeEnabled) {
        this.timeEnabled = timeEnabled;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(int minutes) {
        this.endMinutes = minutes;
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

    private void setReady(boolean ready) {
        this.gameReady = ready;
    }

    public void resetReady(Firebase reference) {
        reference.child("gameReady").setValue(false);
    }

    public boolean isGameLoaded() {
        return gameLoaded;
    }

    private void setLoaded(){
        gameLoaded = true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Map<String, DBTeam> getTeams() {
        return teams;
    }

    //sets ready if its teams are ready
    public void checkReady(final Firebase reference){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DBGame dbGame = mutableData.getValue(DBGame.class);
                boolean ready = true;
                if (dbGame != null) {
                    for (DBTeam dbTeam : dbGame.getTeams().values()) {
                        ready &= dbTeam.isReady();
                    }

                    dbGame.setReady(ready);

                    mutableData.setValue(dbGame);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {

            }
        });
    }

    //sets loaded if its teams are loaded
    public void checkLoaded(final Firebase reference){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DBGame dbGame = mutableData.getValue(DBGame.class);
                boolean loaded = true;
                if (dbGame!=null){
                    for (DBTeam dbTeam: dbGame.getTeams().values()){
                        loaded &= dbTeam.isLoaded();
                    }

                    if (loaded){
                        dbGame.setLoaded();
                    } else {
                        return Transaction.abort();
                    }

                    mutableData.setValue(dbGame);

                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {

            }
        });
    }

    //adds new team to game and puts me on it
    // updates static Team class with new DBTeam
    // updates User's activeGameKey
    public boolean createTeamWithPlayer(final DBTeam dbTeam, final Firebase reference){

        reference.child("teams").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Map<String, DBTeam> dbTeams = currentData.getValue(Map.class);
                if (dbTeams == null) {
                    //should never happen
                    dbTeams = new HashMap<>();
                }
                dbTeams.put(dbTeam.getName(), dbTeam);

                currentData.setValue(dbTeams);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //adds me to the team
                dbTeam.addDBPlayer(reference.child("teams").child(dbTeam.getName()));
            }
        });

        return true;
    }

    public void endGame(Firebase reference){
        reference.child("gameOver").setValue(true);
    }

    public Firebase saveNewGame(){
        Firebase ref = LaserTagApplication.firebaseReference.child("games").push();
        ref.setValue(this);
        return ref;
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
            description.append("to ").append(getEndScore()).append(" points");
        }

        if (getTimeEnabled()) {
            if(getScoreEnabled()) {
                description.append(" or ");
            }
            description.append("until ").append(getEndMinutes()).append(" minutes have elapsed");
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

    public TeamIterator<DBPlayer> makeIterator(){
        return new TeamIterator<DBPlayer>() {
            Iterator<DBPlayer> currentIterator;
            String currentTeamName;
            Iterator<DBTeam> teamIterator = teams.values().iterator();

            @Override
            public boolean hasNext() {
                if ((currentIterator==null || !currentIterator.hasNext()) && teamIterator.hasNext()){
                    DBTeam currentTeam = teamIterator.next();
                    currentTeamName = currentTeam.getName();
                    currentIterator = currentTeam.makeIterator();
                } else {
                    return false;
                }
                return currentIterator.hasNext() || teamIterator.hasNext();
            }

            @Override
            public DBPlayer next() {
                return currentIterator.next();
            }

            @Override
            public String currentTeam(){
                return currentTeamName;
            }
        };
    }
}
