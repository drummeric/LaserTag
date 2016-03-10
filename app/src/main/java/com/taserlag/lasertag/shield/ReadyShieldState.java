package com.taserlag.lasertag.shield;


import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.taserlag.lasertag.R;

public class ReadyShieldState extends ShieldState{

    private boolean firstTime = true;

    @Override
    public void updateUI(Shield shield, TextView shieldTextView, ImageView shieldImageView) {
        if (firstTime){
            firstTime = false;
            shieldTextView.setText("0");
            shieldImageView.setImageResource(R.drawable.shield_ready_animation);
            AnimationDrawable rechargeAnimation = (AnimationDrawable) shieldImageView.getDrawable();
            rechargeAnimation.start();
        }
    }

    @Override
    public boolean deploy(Shield shield, TextView shieldTextView, ImageView shieldImageView) {
        shield.setStrength(shield.getMaxStrength());
        shield.setShieldState(new ActiveShieldState(),shieldTextView,shieldImageView);
        return true;
    }
}
