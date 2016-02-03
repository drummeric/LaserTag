package com.taserlag.lasertag.player;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.taserlag.lasertag.shield.Shield;
import com.taserlag.lasertag.weapon.Weapon;

public class Player extends GenericJson{

    @Key("_id")
    private String id;

    @Key
    private String name;

    @Key
    private int score = 0;

    @Key
    private int health = 100;

    @Key
    private boolean captain = false;

    private boolean primaryWeaponActive = true;
    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;

    private Shield shield;


    public Player(String name) {
        this.name = name;
    }

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

    public String getName() {
        return name;
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

    public boolean isCaptain() {
        return captain;
    }

}
