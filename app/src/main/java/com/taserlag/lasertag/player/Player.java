package com.taserlag.lasertag.player;

import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.weapon.Weapon;

public class Player{

    private String name;

    private int score = 0;

    private int health = 100;

    private boolean captain = false;

    private boolean primaryWeaponActive = true;

    private int[] color;

    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;
    private Shield shield;

    public Player() {}

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, Weapon primaryWeapon, Weapon secondaryWeapon, Shield shield) {
        this.name = name;
        this.primaryWeapon = primaryWeapon;
        this.secondaryWeapon = secondaryWeapon;
        this.shield = shield;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public boolean isPrimaryWeaponActive() {
        return primaryWeaponActive;
    }

    public boolean isCaptain() {
        return captain;
    }

    public int getHealth() {
        return health;
    }

    public int[] getColor() {
        return color;
    }

    public Weapon getActiveWeapon() {
        if (primaryWeaponActive) {
            return primaryWeapon;
        } else {
            return secondaryWeapon;
        }
    }

    public Shield getShield() {
        return shield;
    }

    public void swapWeapon() {
        primaryWeaponActive = !primaryWeaponActive;
    }

    public void setCaptain() {
        captain = true;
    }

    public void resetCaptain() {
        captain = false;
    }


}
