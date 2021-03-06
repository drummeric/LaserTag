package com.taserlag.lasertag.team;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DBTeam implements Comparable<DBTeam>{

    private final static String TAG = "DBTeam";

    private String name = "Team Name";

    private int score = 0;

    private boolean ready = false;

    private boolean loaded = false;

    private boolean captainDead = false;

    private Map<String, DBPlayer> players = new HashMap<>();

    public DBTeam() {}

    public DBTeam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getScore(){
        return score;
    }

    public boolean isReady() {
        return ready;
    }

    private void setReady(boolean ready){
        this.ready = ready;
    }

    public boolean isLoaded() {
        return loaded;
    }

    private void setLoaded(){
        loaded = true;
    }

    public boolean isCaptainDead() {
        return captainDead;
    }

    public void saveCaptainDead(boolean captainDead, Firebase reference) {
        this.captainDead = captainDead;
        reference.child("captainDead").setValue(captainDead);
    }

    public Map<String, DBPlayer> getPlayers() {
        return players;
    }

    public void resetReady(Firebase reference){
        ready = false;
        Game.getInstance().resetReady();
        reference.child("ready").setValue(ready);
    }

    public void checkReady(Firebase reference){
        ready = true;
        reference.child("ready").setValue(ready);

        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DBTeam dbTeam = mutableData.getValue(DBTeam.class);
                boolean ready = true;
                if (dbTeam != null) {
                    for (DBPlayer dbPlayer : dbTeam.getPlayers().values()) {
                        ready &= dbPlayer.isReady();
                    }

                    dbTeam.setReady(ready);

                    mutableData.setValue(dbTeam);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed && dataSnapshot.getValue(DBTeam.class).isReady()) {
                    Game.getInstance().checkReady();
                }
            }
        });
    }

    //sets loaded if its players are loaded
    public void checkLoaded(final Firebase reference){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DBTeam dbTeam = mutableData.getValue(DBTeam.class);
                boolean loaded = true;
                if (dbTeam != null) {
                    for (DBPlayer dbPlayer : dbTeam.getPlayers().values()) {
                        loaded &= dbPlayer.isLoaded();
                    }

                    if (loaded) {
                        dbTeam.setLoaded();
                    } else {
                        return Transaction.abort();
                    }

                    mutableData.setValue(dbTeam);

                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Game.getInstance().checkLoaded();
                }
            }
        });
    }

    //reset player map. ONLY CALLED FROM addDBPlayer!!!!!!!
    // used to avoid bringing players back to teams they left as I join their team
    private void resetPlayers(){
        players = new HashMap<>();
    }

    //called from game lobby in recycler view's list view
    public boolean addDBPlayer(String teamName){
        return addDBPlayer(Game.getInstance().getReference().child("teams").child(teamName));
    }

    /*TODO BUG: If two people try to add themselves to a team that only has
      one spot left, they will pass the first check and then be added asynchronously.
      One person will be added, and one person will not be added and see no error message*/
    public boolean addDBPlayer(final Firebase reference) {
        if (players.size() < Game.getInstance().getMaxTeamSize()) {

            //if you're already on a team, leave it
            if (Team.getInstance().getDBTeam()!=null){
                removeDBPlayer(Team.getInstance().getDBTeamReference());
            }

            reference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    DBTeam dbTeam = currentData.getValue(DBTeam.class);

                    if (dbTeam == null) {
                        dbTeam = DBTeam.this;
                        dbTeam.resetPlayers();
                    }

                    Map<String, DBPlayer> dbPlayers = dbTeam.getPlayers();

                    if (dbPlayers.size() < Game.getInstance().getMaxTeamSize()) {
                        dbPlayers.put(Player.getInstance().getName(), new DBPlayer(Player.getInstance().getName()));
                        currentData.setValue(dbTeam);
                    } else {
                        return Transaction.abort();
                    }

                    return Transaction.success(currentData);
                }

                // Updates static Player and Team's db stuff
                @Override
                public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                    if (committed) {
                        //Update static Team class and reset ready
                        Team.getInstance(DBTeam.this).resetReady();
                        Player.getInstance().join();
                    }
                }
            });
            return true;
        }
        return false;
    }

    //called from game lobby in recycler view's list view
    public boolean removeDBPlayer(String teamName){
        return removeDBPlayer(Game.getInstance().getReference().child("teams").child(teamName));
    }

    public boolean removeDBPlayer(final Firebase reference) {
        reference.child("players").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Map<String, DBPlayer> dbPlayers = currentData.getValue(Map.class);
                if (dbPlayers == null) {
                    //should never happen
                    dbPlayers = new HashMap<>();
                }

                if (dbPlayers.containsKey(Player.getInstance().getName())) {
                    dbPlayers.remove(Player.getInstance().getName());
                    currentData.setValue(dbPlayers);
                } else {
                    return Transaction.abort();
                }

                return Transaction.success(currentData);
            }

            // Deletes team from DB if empty
            // Resets static Team and Player
            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    Map<String, DBPlayer> dbPlayers = currentData.getValue(Map.class);
                    if (dbPlayers == null){
                        reference.setValue(null);
                    }
                    //resets Team's dbTeam and Player's dbPlayer
                    Team.getInstance().leaveTeam();
                }
            }
        });
        return true;
    }

    public void incrementScore(final int value, final Firebase reference) {
        reference.child("score").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue(Integer.class) == null) {
                    mutableData.setValue(0);
                } else {
                    mutableData.setValue(mutableData.getValue(Integer.class) + value);
                }
                if (Game.getInstance().getScoreEnabled()&&Game.getInstance().getEndScore()<=mutableData.getValue(Integer.class)){
                    Game.getInstance().saveGameOver();
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.i(TAG, "Successfully incremented score for team " + reference.getKey());
                Game.getInstance().incrementTotalScore(value);
            }
        });
    }

    @Override
    public boolean equals(Object dbTeam){
        return (dbTeam instanceof DBTeam) && (((DBTeam) dbTeam).getName().equals(name));
    }

    //resets calling player's captain boolean and selects new captain
    public void resetTeamCaptain(){
        Player.getInstance().saveCaptain(false);
        setTeamCaptain();
    }

    //only called directly at the start of the game
    public void setTeamCaptain(){
        Random random = new Random();
        List<String> keys = new ArrayList<String>(players.keySet());
        String randomKey = keys.get(random.nextInt(keys.size()));
        players.get(randomKey).getPlayerStats().saveCaptain(true, Game.getInstance().getReference().child("teams").child(name).child("players").child(randomKey).child("playerStats"));
    }

    public Iterator<DBPlayer> makeIterator() {
        return players.values().iterator();
    }

    @Override
    public int compareTo(DBTeam other) {
        if (name.equals(other.getName())) {
            return 0;
        } else if (getScore() != other.getScore()) {
            return other.getScore() - getScore();
        } else {
            return 0;
        }
    }
}
