package com.taserlag.lasertag.team;

import com.taserlag.lasertag.player.Player;

import java.util.List;

public class GroupTeam extends Team{

    private List<Player> players;
    @Override
    public boolean addPlayer(Player p) {
        if (findPlayer(p) == -1){
            players.add(p);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removePlayer(Player p) {
        if (findPlayer(p) == -1){
            return false;
        } else {
            players.remove(p);
            return true;
        }
    }

    @Override
    public boolean isValid() {

        return (players.size() > 0);
    }

    @Override
    public boolean setCaptain(Player p) {
        if (findPlayer(p) == -1){
            return false;
        }

        captain = p;
        return true;
    }

    private int findPlayer(Player p){
        return players.indexOf(p);
    }
}
