package com.taserlag.lasertag.game;

public interface GameFollower {

    void notifyGameUpdated();

    void notifyGameLoaded();

    void notifyGameReady();

    void notifyGameOver();

    void notifyGameDeleted();

    void notifyGameScoreUpdated();
}
