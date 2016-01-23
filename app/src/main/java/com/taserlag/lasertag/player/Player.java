package com.taserlag.lasertag.player;

import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.weapon.Weapon;

public class Player {

    private String name;
    private int score = 0;
    private int health = 100;
    private Shield shield;
    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;
    private boolean primaryWeaponActive = true;

    public Player(String name, Weapon primaryWeapon, Weapon secondaryWeapon, Shield shield) {
        this.name = name;
        this.primaryWeapon = primaryWeapon;
        this.secondaryWeapon = secondaryWeapon;
        this.shield = shield;
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
        if (primaryWeaponActive) {
            primaryWeaponActive = false;
        } else {
            primaryWeaponActive  = true;
        }
    }

}
