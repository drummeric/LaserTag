package com.taserlag.lasertag.game;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.taserlag.lasertag.team.Team;

import java.util.HashMap;
import java.util.Map;

@ParseClassName("Game")
public class Game extends ParseObject {

    private Map<String,Team> teams = new HashMap<>();

    public Game() {}

    public String getGameType() {
        return getString("gameType");
    }

    public void setGameType(GameType gameType) {
        put("gameType", gameType.toString());
    }

    public boolean getScoreEnabled() {
        return getBoolean("scoreEnabled");
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        put("scoreEnabled", scoreEnabled);
    }

    public int getScore() {
        return getInt("score");
    }

    public void setScore(int score) {
        put("score", score);
    }

    public boolean getTimeEnabled() {
        return getBoolean("timeEnabled");
    }

    public void setTimeEnabled(boolean timeEnabled) {
        put("timeEnabled", timeEnabled);
    }

    public int getMinutes() {
        return getInt("minutes");
    }

    public void setMinutes(int minutes) {
        put("minutes", minutes);
    }

    public int getTeamSize() {
        return getInt("teamSize");
    }

    public void setTeamSize(int size) {
        put("teamSize", size);
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

    public boolean addTeam(Team team){
        if (teams.containsKey(team.getName())){
            return false;
        }

        teams.put(team.getName(), team);
        return true;
    }

    public boolean removeTeam(Team team){
        return teams.remove(team.getName()) != null;
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

        if (!GameType.decodeType(getGameType()).equals(GameType.FFA)){
            description.append(" The maximum team size is ").append(getTeamSize()).append(".");
        }

        return description.toString();
    }
}
