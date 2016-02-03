package com.taserlag.lasertag.game;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Game extends GenericJson {
    @Key("_id")
    private String id;

    @Key
    private String gameType;

    @Key
    private boolean scoreEnabled;

    @Key
    private int score;

    @Key
    private boolean timeEnabled;

    @Key
    private int minutes;

    @Key
    private int maxTeamSize;

    @Key
    private boolean privateMatch;

    @Key
    private boolean friendlyFire;

    @Key
    private List<Team> teams = new ArrayList<>();

    public Game() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType.toString();
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

    public boolean addTeam(Team team){
        if (teams.contains(team)){
            return false;
        }
        team.setMaxTeamSize(maxTeamSize);
        teams.add(team);
        return true;
    }

    public boolean removeTeam(Team team){
        return teams.remove(team);
    }

    public List<Team> getTeams() {
        return teams;
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
            description.append(" The maximum team size is ").append(getMaxTeamSize()).append(".");
        }

        return description.toString();
    }
}
