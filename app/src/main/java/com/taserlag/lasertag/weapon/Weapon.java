package com.taserlag.lasertag.weapon;

public abstract class Weapon {

    protected int strength;
    protected long fireRate;
    protected int excessAmmo;
    protected int clipSize;
    protected int currentClipAmmo;

    public int getCurrentClipAmmo() {
        return currentClipAmmo;
    }

    public int getExcessAmmo() {
        return excessAmmo;
    }

    public boolean fire() {
        if (currentClipAmmo != 0) {
            currentClipAmmo--;
            return true;
        }
        return false;
    }

    public void reload() {
        // This logic can probably be MUCH better
        if (excessAmmo > 0) {
            if (excessAmmo + currentClipAmmo >= clipSize) {
                excessAmmo -= (clipSize - currentClipAmmo);
                currentClipAmmo = clipSize;
            } else {
                currentClipAmmo += excessAmmo;
                excessAmmo = 0;
            }
        }
    }
}