package com.taserlag.lasertag.game;

import com.taserlag.lasertag.team.GroupTeam;
import com.taserlag.lasertag.team.Team;

public class VIPGame extends Game{
    @Override
    public boolean addTeam(Team team) {
        if (team instanceof GroupTeam && teams.indexOf(team) == -1){
            teams.add(team);
            return true;
        } else {
            return false;
        }
    }

    public boolean checkTeams(){
        for (Team t: teams){
            if (!t.hasCaptain()){
                return false;
            }
        }

       return super.checkTeams();
    }
}
