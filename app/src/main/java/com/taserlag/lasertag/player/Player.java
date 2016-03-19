package com.taserlag.lasertag.player;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.weapon.FastWeapon;
import com.taserlag.lasertag.weapon.StrongWeapon;
import com.taserlag.lasertag.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

//ties 3 player classes together and keeps player stuff not in DB
public class Player{

    private static final String TAG = "Player";

    //health in DB is actually realHealth + shieldStrength
    private int realHealth = 100;

    private boolean mPrimaryWeaponActive = true;

    private int mTotalShots;
    private int mTotalHits;

    private Weapon mPrimaryWeapon = new FastWeapon();
    private Weapon mSecondaryWeapon = new StrongWeapon();
    private Shield mShield = new Shield();
    private static DBPlayer dbPlayer;
    private static DBUser dbUser;
    private static Firebase dbPlayerReference;
    private static ValueEventListener playerListener;
    private static ValueEventListener userListener;
    private static List<PlayerFollower> followers = new ArrayList<>();

    private static Player instance = null;

    public static Player getInstance(){
        if (instance==null){
            instance = new Player();
            playerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Player has been successfully updated for player:" + instance.getName());
                    DBPlayer newDBPlayer = dataSnapshot.getValue(DBPlayer.class);
                    if (newDBPlayer!=null && dbPlayer!=null) {
                        if (newDBPlayer.getHealth() < dbPlayer.getHealth()){
                            instance.decrementHealth(newDBPlayer.getHealth());
                            notifyHealthFollowers();

                            if (newDBPlayer.getHealth()==0){
                                dbPlayer.getPlayerStats().incrementDeaths(dbPlayerReference.child("playerStats"));
                            }
                        }

                        if (newDBPlayer.getPlayerStats().getColor()!=dbPlayer.getPlayerStats().getColor()){
                            dbUser.saveColor(newDBPlayer.getPlayerStats().getColor());
                        }

                    }
                    dbPlayer = newDBPlayer;
                    notifyFollowers();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, "Player failed to update for player: " + instance.getName(), firebaseError.toException());
                }
            };

            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dbUserSnapshot) {
                    Log.i(TAG, "Player user has been successfully updated for user: " + LaserTagApplication.getUid());
                    DBUser updatedUser = dbUserSnapshot.getValue(DBUser.class);

                    if (updatedUser!=null) {
                        dbUser = updatedUser;
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, "Player user failed to update for user: " + LaserTagApplication.getUid());
                }
            };
            
            LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.getUid()).addValueEventListener(userListener);
        }
        return instance;
    }

    public String getName() {
        return dbUser.getUsername();
    }

    public int getTotalShots() {
        return mTotalShots;
    }

    public void setTotalShots(int mTotalShots) {
        this.mTotalShots = mTotalShots;
    }

    public void incTotalShots() {
        mTotalShots++;
    }

    public int getTotalHits() {
        return mTotalHits;
    }

    public void setTotalHits(int mTotalHits) {
        this.mTotalHits = mTotalHits;
    }

    public void incTotalHits() {
        mTotalHits++;
    }

    private void saveHitPercentage() {
        dbPlayer.getPlayerStats().saveHitPercentage( mTotalShots != 0 ? (double)mTotalHits/mTotalShots : 0, dbPlayerReference.child("playerStats"));
    }

    public int getScore() {
        return dbPlayer.getPlayerStats().getScore();
    }

    public boolean isCaptain() {
        return dbPlayer.getPlayerStats().isCaptain();
    }

    public void setCaptain(boolean captain) {
        dbPlayer.getPlayerStats().setCaptain(captain);
    }

    public int[] getColor() {
        return dbUser.getColor();
    }

    public void archiveGame(String gameKey){
        saveHitPercentage();
        dbUser.archiveGame(gameKey);
    }

    //call when you leave a team/game
    public void leave(){
        if (dbPlayerReference!=null) {
            dbPlayerReference.removeEventListener(playerListener);
        }
        dbPlayerReference = null;
        dbPlayer = null;
    }

    //call when you join a team
    public void join(){
        if (dbPlayerReference!=null) {
            dbPlayerReference.removeEventListener(playerListener);
        }
        dbPlayerReference = Team.getInstance().getDBTeamReference().child("players").child(getName());
        dbPlayerReference.addValueEventListener(playerListener);

        //so we don't have to wait for the listener to update dbPlayer
        dbPlayer = new DBPlayer(dbUser.getUsername());
    }

    public boolean isLoaded() {
        return dbPlayer.isLoaded();
    }

    public void loadUp() {
        dbPlayer.loadUp(dbPlayerReference);
    }

    public boolean isReady() {
        return dbPlayer.isReady();
    }

    public void readyUp() {
        dbPlayer.readyUp(dbPlayerReference);
    }

    public void resetReady(){
        dbPlayer.resetReady(dbPlayerReference);
    }

    public boolean isPrimaryWeaponActive() {
        return mPrimaryWeaponActive;
    }

    public DBPlayer getDBPlayer(){
        return dbPlayer;
    }

    public Weapon retrieveActiveWeapon() {
        if (mPrimaryWeaponActive) {
            return mPrimaryWeapon;
        } else {
            return mSecondaryWeapon;
        }
    }

    // decrement realHealth by the damage not absorbed by the shield
    public boolean decrementHealth(int value){
        int realHealthDamage = mShield.decStrength(realHealth + mShield.getStrength() - value);
        if (realHealthDamage!=0){
            realHealth -= realHealthDamage;
        }
        return realHealth == 0;
    }

    public int getRealHealth(){
        return realHealth;
    }

    // decrement other people's health, cannot hurt yourself
    // returns false if you try to decrement your own health
    public boolean decrementHealthAndIncMyScore(final int value,final Firebase reference){
        return dbPlayer.decrementHealthAndIncMyScore(value, reference, dbPlayerReference);
    }

    public boolean deployShield(TextView shieldTextView, ImageView shieldImageView){
        boolean deployed = mShield.deploy(shieldTextView, shieldImageView);
        if (deployed) {
            dbPlayer.incrementHealth(mShield.getStrength(), dbPlayerReference);
        }
        return deployed;
    }

    public Shield getShield() {
        return mShield;
    }

    public void swapWeapon() {
        mPrimaryWeaponActive = !mPrimaryWeaponActive;
    }

    //reinits shields and weapons to new objects
    public static void reset(){
        instance = new Player();
    }

    public static void respawn(){
        instance = new Player();
        dbPlayer.resetHealth(dbPlayerReference);
    }

    public static void disconnect(){
        instance = null;
        dbUser = null;
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.getUid()).removeEventListener(userListener);
    }

    private static void notifyFollowers() {
        for (PlayerFollower follower : followers) {
            follower.notifyPlayerUpdated();
        }
    }

    //notify followers that health has decreased
    private static void notifyHealthFollowers() {
        for (PlayerFollower follower : followers) {
            follower.notifyPlayerHealthDecremented();
        }
    }

    public void registerForUpdates(PlayerFollower follower) {
        if (!followers.contains(follower)) {
            followers.add(follower);

            if (dbPlayer != null) {
                notifyFollowers();
            }
        }
    }

    public void unregisterForUpdates(PlayerFollower follower) {
        followers.remove(follower);
    }

}
