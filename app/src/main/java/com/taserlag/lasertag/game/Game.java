package com.taserlag.lasertag.game;

import com.taserlag.lasertag.team.Team;

import java.util.List;

public abstract class Game {

    protected List<Team> teams;

    private long currentTime;

    private long startTime = 0;

    public int score = 10;
    public int minutes = 5;

    public boolean scoreEnabled = false;
    public boolean timeEnabled = true;
    public boolean privateMatch = false;
    public boolean friendlyFire = true;

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

        if (privateMatch){
            description.append("Private ");
        }

        description.append(getGameType()).append(" game ");

        if (scoreEnabled){
            description.append("to ").append(score).append(" points");
        }

        if (timeEnabled){
            if(scoreEnabled){
                description.append(" or ");
            }
            description.append("until ").append(minutes).append(" minutes have elapsed");

        }

        description.append(". Friendly fire is ");

        if (friendlyFire){
            description.append("enabled.");
        } else {
            description.append("disabled.");
        }

        return description.toString();
    }
}
