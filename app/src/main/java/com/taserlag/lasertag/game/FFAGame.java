package com.taserlag.lasertag.game;

import com.taserlag.lasertag.team.FFATeam;
import com.taserlag.lasertag.team.Team;

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
}
