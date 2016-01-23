package com.taserlag.lasertag.shield;

public abstract class Shield {

    protected int maxStrength;
    protected int strength;
    protected long rechargeTime;

    public void reset() {
        strength = maxStrength;
    }

    public void decStrength(int damage) {
        if (strength > damage) {
            strength -= damage;
        } else {
            strength = 0;
        }
    }

}
