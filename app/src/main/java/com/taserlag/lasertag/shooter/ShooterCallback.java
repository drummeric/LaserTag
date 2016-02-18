package com.taserlag.lasertag.shooter;


public interface ShooterCallback {

    void onFinishShoot(String playerHit);

    void updateGUI();
}
