package com.taserlag.lasertag.game;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game{

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

    private Map<String, List<String>> fullKeys = new HashMap<>();

    public Game() {}

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

    public Map<String, List<String>> getFullKeys() {
        return fullKeys;
    }

    public void enableListeners(Firebase reference){

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

    public boolean createTeamWithGlobalPlayer(final Team team, final Firebase gameReference){
        if (teamNameExists(team)){
            return false;
        }
        team.setMaxTeamSize(maxTeamSize);
        addGlobalPlayer(team.getName() + ":~", gameReference);

        return true;
    }

    //teamFullKey ends in ":~" for new team
    public boolean addGlobalPlayer(final String teamFullKey, final Firebase gameReference){
        String currentTeamFullKey = findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid());

        if (!currentTeamFullKey.equals("")){
            // player is on a team
            removeGlobalPlayer(currentTeamFullKey, gameReference);
        } else if (currentTeamFullKey.equals(teamFullKey)) {
            //global player on team already, don't need to re-add
            return true;
        } else if (!teamFullKey.endsWith(":~") && fullKeys.get(teamFullKey).size() >= maxTeamSize){
            //team full
            return false;
        }

        //lol
        final Game thisGame = this;

        gameReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    //should never happen
                    currentData.setValue(thisGame);
                } else {
                    Game game = currentData.getValue(Game.class);

                    //if the team isn't in the game's map
                    if (game.getFullKeys().get(teamFullKey) == null) {
                        Team team = new Team(teamFullKey.split(":~")[0]);
                        team.setMaxTeamSize(maxTeamSize);

                        // add global player to new team
                        List<String> playerKey = new ArrayList<String>();
                        playerKey.add(LaserTagApplication.firebaseReference.getAuth().getUid());

                        // if this is a new team
                        if (teamFullKey.endsWith(":~")) {
                            Firebase ref = LaserTagApplication.firebaseReference.child("teams").push();
                            ref.setValue(team);
                            game.getFullKeys().put(teamFullKey + ref.getKey(), playerKey);
                        } else {
                            //existing team not on game map (due to database propogation)
                            LaserTagApplication.firebaseReference.child("teams").child(teamFullKey).setValue(team);
                            game.getFullKeys().put(teamFullKey, playerKey);
                        }

                    } else {
                        game.getFullKeys().get(teamFullKey).add(LaserTagApplication.firebaseReference.getAuth().getUid());
                    }
                    currentData.setValue(game);
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
                            .setValue(gameReference.getKey());
                    restoreGameMap(currentData.getValue(Game.class));
                }
            }
        });

        return true;
    }

    public boolean removeGlobalPlayer(final String teamFullKey, Firebase gameReference) {
        //lol
        final Game thisGame = this;

        gameReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(thisGame);
                } else {
                    Game game = currentData.getValue(Game.class);
                    game.getFullKeys().get(teamFullKey).remove(LaserTagApplication.firebaseReference.getAuth().getUid());

                    //empty team -> remove team from database
                    if (game.getFullKeys().get(teamFullKey).isEmpty()) {
                        game.getFullKeys().remove(teamFullKey);
                    }

                    currentData.setValue(game);
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
                            .setValue("");
                    LaserTagApplication.firebaseReference
                            .child("users")
                            .child(LaserTagApplication.firebaseReference.getAuth().getUid())
                            .child("player")
                            .child("ready")
                            .setValue(false);
                    restoreGameMap(currentData.getValue(Game.class));
                }
            }
        });
        return true;
    }

    public boolean removeGlobalPlayer(Firebase gameReference){
        return removeGlobalPlayer(findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid()),gameReference);
    }

    public void restoreGameMap(Game game){
        this.fullKeys = game.getFullKeys();
    }

    private boolean teamNameExists(Team team){
        for(String fullKey : fullKeys.keySet()){
            if (team.getName().equals(fullKey.split(":~")[0])){
                return true;
            }
        }
        return false;
    }

    private String getTeamFullKey(Team team){
        for(String fullKey : fullKeys.keySet()){
            if (team.getName().equals(fullKey.split(":~")[0])){
                return fullKey;
            }
        }
        return null;
    }

    //Returns teamfullkey of team to which player belongs
    // else null
    public String findPlayer(String playerUID){

        for (Map.Entry<String, List<String>> entry: fullKeys.entrySet()){
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

        if (getPrivateMatch()) {
            description.append("Private ");
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
