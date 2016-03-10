package com.taserlag.lasertag.player;


public interface PlayerFollower {

    public void notifyPlayerUpdated();

    //notify followers that health has decreased
    public void notifyPlayerHealthUpdated();
}
