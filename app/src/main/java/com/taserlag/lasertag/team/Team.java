package com.taserlag.lasertag.team;

import com.taserlag.lasertag.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Team{

    private String name = "Team Name";

    private List<Player> players = new ArrayList<>();

    private int maxTeamSize = 1;

    public Team() {}

    public Team(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public boolean equals(Object team){
        return (team instanceof Team) && (((Team) team).getName().equals(name));
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
