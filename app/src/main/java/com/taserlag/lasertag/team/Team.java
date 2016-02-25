package com.taserlag.lasertag.team;

public class Team{

    private String name = "Team Name";

    private int maxTeamSize = 1;

    private int score = 0;

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

    public int getScore(){
        return score;
    }

    @Override
    public boolean equals(Object team){
        return (team instanceof Team) && (((Team) team).getName().equals(name));
    }

    public void setCaptain(){
       //todo write this function
    }
}
