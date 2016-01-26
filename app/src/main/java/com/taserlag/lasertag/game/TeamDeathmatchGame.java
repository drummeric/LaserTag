package com.taserlag.lasertag.game;

import com.parse.ParseClassName;
import com.taserlag.lasertag.team.GroupTeam;
import com.taserlag.lasertag.team.Team;

@ParseClassName("TDMGame")
public class TeamDeathmatchGame extends Game{

    @Override
    public boolean addTeam(Team team) {
        if (team instanceof GroupTeam && teams.indexOf(team) == -1){
            teams.add(team);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getGameType() {
        return "Team Deathmatch";
    }
}
