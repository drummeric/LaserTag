package com.taserlag.lasertag.game;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.taserlag.lasertag.team.Team;

import java.util.List;

@ParseClassName("Game")
public abstract class Game extends ParseObject {

    protected List<Team> teams;

    private long currentTime;

    private long startTime = 0;

    public Game() {
    }

    public ParseUser getUser() {
        return getUser();
    }

    public int getScore() {
        return getInt("score");
    }

    public void setScore(int score) {
        put("score", score);
    }

    public int getMinutes() {
        return getInt("minutes");
    }

    public void setMinutes(int minutes) {
        put("minutes", minutes);
    }

    public boolean getScoreEnabled() {
        return getBoolean("scoreEnabled");
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        put("scoreEnabled", scoreEnabled);
    }

    public boolean getTimeEnabled() {
        return getBoolean("timeEnabled");
    }

    public void setTimeEnabled(boolean timeEnabled) {
        put("timeEnabled", timeEnabled);
    }

    public boolean getPrivateMatch() {
        return getBoolean("privateMatch");
    }

    public void setPrivateMatch(boolean privateMatch) {
        put("privateMatch", privateMatch);
    }

    public boolean getFriendlyFire() {
        return getBoolean("friendlyFire");
    }

    public void setFriendlyFire(boolean friendlyFire) {
        put("friendlyFire", friendlyFire);
    }

    public abstract boolean addTeam(Team team);

    public abstract String getGameType();

    protected int findTeam(Team team){

        return teams.indexOf(team);
    }

    public boolean checkTeams(){
        for (Team t: teams){
            if (!t.isValid()){
                return false;
            }
        }

        return true;
    }

    public boolean start(){
        if (checkTeams()) {
            startTime = System.currentTimeMillis();
        } else {
            return false;
        }

        //do other start stuff

        return true;
    }

    public long getTime(){
        if (startTime != 0){
            currentTime = System.currentTimeMillis() - startTime;
            return currentTime;
        } else {
            return -1;
        }
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

        return description.toString();
    }
}
