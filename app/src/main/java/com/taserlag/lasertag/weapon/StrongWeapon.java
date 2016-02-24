package com.taserlag.lasertag.weapon;

public class StrongWeapon extends Weapon {

    public StrongWeapon() {
        strength = 20;
        excessAmmo = 40;
        clipSize = 10;
        currentClipAmmo = 10;
        fireRate = 1000;
    }

    @Override
    public String toString(){
        return "Strong";
    }

}
