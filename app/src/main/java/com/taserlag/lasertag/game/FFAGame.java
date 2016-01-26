package com.taserlag.lasertag.game;

import com.parse.ParseClassName;
import com.taserlag.lasertag.team.FFATeam;
import com.taserlag.lasertag.team.Team;

@ParseClassName("FFAGame")
public class FFAGame extends Game{
    @Override
    public boolean addTeam(Team team) {
        if (team instanceof FFATeam && teams.indexOf(team) == -1){
            teams.add(team);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getGameType() {
        return "Free for All";
    }
}
