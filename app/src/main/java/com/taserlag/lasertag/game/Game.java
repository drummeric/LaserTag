package com.taserlag.lasertag.game;

import com.google.api.client.json.GenericJson;
import com.taserlag.lasertag.team.Team;

import java.util.LinkedHashMap;
import java.util.Map;

public class Game extends GenericJson {

    private Map<String,Team> teams = new LinkedHashMap<>();

    public Game() {}

    public String getGameType() {
        return (String) get("gameType");
    }

    public void setGameType(GameType gameType) {
        put("gameType", gameType.toString());
    }

    public boolean getScoreEnabled() {
        return (boolean) get("scoreEnabled");
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        put("scoreEnabled", scoreEnabled);
    }

    public int getScore() {
        return (int) get("score");
    }

    public void setScore(int score) {
        put("score", score);
    }

    public boolean getTimeEnabled() {
        return (boolean) get("timeEnabled");
    }

    public void setTimeEnabled(boolean timeEnabled) {
        put("timeEnabled", timeEnabled);
    }

    public int getMinutes() {
        return (int) get("minutes");
    }

    public void setMinutes(int minutes) {
        put("minutes", minutes);
    }

    public int getMaxTeamSize() {
        return (int) get("maxTeamSize");
    }

    public void setMaxTeamSize(int size) {
        put("maxTeamSize", size);
    }

    public boolean getPrivateMatch() {
        return (boolean) get("privateMatch");
    }

    public void setPrivateMatch(boolean privateMatch) {
        put("privateMatch", privateMatch);
    }

    public boolean getFriendlyFire() {
        return (boolean) get("friendlyFire");
    }

    public void setFriendlyFire(boolean friendlyFire) {
        put("friendlyFire", friendlyFire);
    }

    public boolean addTeam(Team team){
        if (teams.containsKey(team.getName())){
            return false;
        }
        team.setMaxTeamSize(getMaxTeamSize());
        teams.put(team.getName(), team);
        return true;
    }

    public boolean removeTeam(Team team){
        return teams.remove(team.getName()) != null;
    }

    public Map<String, Team> getTeams() {
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
