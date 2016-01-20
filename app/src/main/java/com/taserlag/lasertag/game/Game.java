package com.taserlag.lasertag.game;

import com.taserlag.lasertag.team.Team;

import java.util.List;

public abstract class Game {

    protected List<Team> teams;

    private long currentTime;

    private long startTime = 0;



    public abstract boolean addTeam(Team team);

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
}
