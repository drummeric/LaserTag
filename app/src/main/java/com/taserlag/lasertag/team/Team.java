package com.taserlag.lasertag.team;

public abstract class Team {

    protected Player captain;

    public abstract boolean addPlayer(Player p);

    public abstract boolean removePlayer(Player p);

    public abstract boolean isValid();

    public abstract boolean setCaptain(Player p);

    public boolean hasCaptain(){

        return (captain != null);
    }

}
