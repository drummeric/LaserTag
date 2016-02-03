package com.taserlag.lasertag.team;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.taserlag.lasertag.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Team extends GenericJson{

    @Key("_id")
    private String id;

    @Key
    private List<Player> players = new ArrayList<>();

    @Key
    private String name = "Team Name";

    @Key
    private int maxTeamSize = 1;

    public Team(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
