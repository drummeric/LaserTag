package com.taserlag.lasertag.weapon;

public abstract class Weapon {

    protected int strength;
    protected long fireRate;
    protected int excessAmmo;
    protected int clipSize;
    protected int currentClipAmmo;
    private long lastShotTime = 0;

    public int getCurrentClipAmmo() {
        return currentClipAmmo;
    }

    public int getExcessAmmo() {
        return excessAmmo;
    }

    public int getStrength(){
        return strength;
    }

    // if you have ammo and the time elapsed since the last shot is
    // greater than the fireRate of the weapon, canShoot
    private boolean canShoot() {
        return (currentClipAmmo != 0) && (System.currentTimeMillis() - lastShotTime >= fireRate);
    }

    // only fires if it can, returns success/failure
    // save shot time and dec ammo
    public boolean fire(){
        boolean canShoot = canShoot();
        if (canShoot) {
            lastShotTime = System.currentTimeMillis();
            currentClipAmmo--;
        }
        return canShoot;
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
