package com.taserlag.lasertag.team;

import com.taserlag.lasertag.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private List<Player> players = new ArrayList<>();
    private String name = "Team Name";
    private int maxTeamSize;

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean addPlayer(Player p){
        if (players.size()>=maxTeamSize || players.contains(p)){
            return false;
        }

        players.add(p);

        return true;
    }

    public boolean removePlayer(Player p){
        return players.remove(p);
    }

    public void setCaptain(){
        for (Player p : players){
            p.resetCaptain();
        }

        int rand = (int) (Math.random() * players.size());
        players.get(rand).setCaptain();
    }
}
