package com.taserlag.lasertag.weapon;

public class FastWeapon extends Weapon {

    public FastWeapon() {
        strength = 5;
        excessAmmo = 150;
        clipSize = 50;
        currentClipAmmo = 50;
        fireRate = 200;
    }

    @Override
    public String toString(){
        return "Fast";
    }

}
