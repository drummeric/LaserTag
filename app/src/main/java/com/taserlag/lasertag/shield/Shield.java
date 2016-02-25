package com.taserlag.lasertag.shield;

import com.taserlag.lasertag.activity.FPSActivity;

public class Shield {

    protected int maxStrength = 100;
    protected int strength = 0;
    protected long rechargeTime = 10000;
    private long failureTime = 0;
    private boolean active = false;

    public int getStrength() {
        return strength;
    }

    public boolean deploy(){
        boolean canDeploy = strength == 0 && (System.currentTimeMillis()-failureTime>=rechargeTime);
        if (canDeploy){
            strength = maxStrength;
            active = true;
        }
        return canDeploy;
    }

    //returns remainder of damage not absorbed by shield
    public int decStrength(int damage) {
        if (strength > damage) {
            strength -= damage;
            FPSActivity.updateShieldUI();
            return 0;
        } else {
            int remainder = damage - strength;

            // shield isn't active, don't update UI or failure time
            if (active) {
                failureTime = System.currentTimeMillis();
                strength = 0;
                FPSActivity.updateShieldUI();
                FPSActivity.rechargeShieldImage();
            }
            active = false;
            return remainder;
        }
    }

}
