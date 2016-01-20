package com.taserlag.lasertag.team;

public class FFATeam extends Team{

    @Override
    public boolean addPlayer(Player p) {
        if (captain == null){
            captain = p;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removePlayer(Player p) {
        if (captain == p){
            captain = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isValid() {

        return (captain != null);
    }

    @Override
    public boolean setCaptain(Player p) {
        return (captain == p);
    }
}
