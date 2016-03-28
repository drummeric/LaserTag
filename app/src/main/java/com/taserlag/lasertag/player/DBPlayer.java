package com.taserlag.lasertag.player;

import android.location.Location;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameType;
import com.taserlag.lasertag.team.Team;
// player stuff stored in DB for use IN game (not after game)
public class DBPlayer implements Comparable<DBPlayer>{

    private static final String TAG = "DBPlayer";

    private String name;

    private int health = 100;

    private boolean ready = false;

    private boolean loaded = false;

    private DBPlayerStats playerStats = new DBPlayerStats();

    private double latitude = 0;

    private double longitude = 0;

    public DBPlayer() {}

    public DBPlayer(String name) {
        this.name = name;
        playerStats.setColor(Player.getInstance().getColor());
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    private void setHealth(int health){
        this.health = health;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public void loadUp(Firebase reference){
        loaded = true;
        reference.child("loaded").setValue(loaded);

        Team.getInstance().checkLoaded();
    }

    public void readyUp(Firebase reference) {
        ready = true;
        reference.child("ready").setValue(ready);
        Team.getInstance().checkReady();
    }

    public DBPlayerStats getPlayerStats() {
        return playerStats;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void saveLocation(Location location, Firebase reference) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        reference.child("latitude").setValue(this.latitude);
        reference.child("longitude").setValue(this.longitude);
    }

    public void resetReady(Firebase reference){
        ready = false;
        Team.getInstance().resetReady();
        reference.child("ready").setValue(ready);
    }

    public void resetHealth(Firebase reference){
       reference.child("health").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue(Integer.class)!=null){
                    mutableData.setValue(100);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.i(TAG, "Successfully reset health for player " + LaserTagApplication.getUid());
            }
        });
    }

    public void incrementHealth(final int value, Firebase reference){
        // adds maxShieldStrength to health in DB
        reference.child("health").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData healthData) {
                if (healthData.getValue(Integer.class) != null) {
                    healthData.setValue(healthData.getValue(Integer.class) + value);
                }
                return Transaction.success(healthData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    // decrement other people's health, cannot hurt yourself
    // returns false if you try to decrement your own health
    // reference points to DBPlayer in Team's player map
    // my reference points to me in that map
    public boolean decrementHealthAndIncMyScore(final int value,final Firebase reference, final Firebase myReference){
        //since team has a map of player names to dbPlayers, and reference is a reference to the DB player,
        // reference.getKey() should return the player name.
        boolean hitYourself = Player.getInstance().getName().equals(reference.getKey());

        if (!hitYourself) {
            reference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {

                    DBPlayer dbPlayer = mutableData.getValue(DBPlayer.class);
                    int health = dbPlayer.getHealth();
                    boolean isCaptain = dbPlayer.getPlayerStats().isCaptain();
                    if (health > 0) {
                        health -= value;
                        if (health > 0) {
                            dbPlayer.setHealth(health);
                        } else {
                            //they're dead, inc my score and kills by 1
                            // if !(VIP mode && they're not a captain)
                            if (!(Game.getInstance().getGameType() == GameType.VIP && !isCaptain) && !reference.getParent().getParent().getKey().equals(Team.getInstance().getName())) {
                                playerStats.incrementScore(10, myReference.child("playerStats"));
                            }
                            playerStats.incrementKills(myReference.child("playerStats"));
                            dbPlayer.setHealth(0);

                        }
                    }
                    mutableData.setValue(dbPlayer);

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Successfully decremented health for player " + reference.getKey());
                }
            });
        }

        return !hitYourself;
    }

    @Override
    public int compareTo(DBPlayer other) {
        if (name.equals(other.getName())) {
            return 0;
        } else if (playerStats.getScore() != other.getPlayerStats().getScore()) {
            return other.getPlayerStats().getScore() - playerStats.getScore();
        } else if (playerStats.getKills() != other.getPlayerStats().getKills()) {
            return other.getPlayerStats().getKills() - playerStats.getKills();
        } else if (playerStats.getDeaths() != other.getPlayerStats().getDeaths()) {
            return getPlayerStats().getDeaths() - other.playerStats.getDeaths();
        } else {
            return 0;
        }
    }
}
