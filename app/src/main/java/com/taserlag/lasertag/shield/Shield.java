package com.taserlag.lasertag.shield;

import android.widget.ImageView;
import android.widget.TextView;

public class Shield {

    protected int maxStrength = 100;
    protected int strength = 0;
    protected long rechargeTime = 10000;
    private ShieldState mShieldState = new ReadyShieldState();

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getMaxStrength() {
        return maxStrength;
    }

    //returns remainder of damage not absorbed by shield
    public int decStrength(int damage) {
        if (strength > damage) {
            strength -= damage;
            return 0;
        } else {
            //shield is dead/has died
            int remainder = damage - strength;
            strength = 0;
            return remainder;
        }
    }

    public void setShieldState(ShieldState state, TextView shieldTextView, ImageView shieldImageView){
        mShieldState = state;
        updateUI(shieldTextView, shieldImageView);
    }

    public void updateUI(TextView shieldTextView, ImageView shieldImageView){
        mShieldState.updateUI(this, shieldTextView, shieldImageView);
    }

    public boolean deploy(TextView shieldTextView, ImageView shieldImageView){
        return mShieldState.deploy(this, shieldTextView, shieldImageView);
    }
}
