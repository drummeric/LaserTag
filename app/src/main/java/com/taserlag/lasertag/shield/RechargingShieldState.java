package com.taserlag.lasertag.shield;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.player.Player;

public class RechargingShieldState extends ShieldState {
    private boolean firstTime = true;
    @Override
    public void updateUI(final Shield shield, final TextView shieldTextView, final ImageView shieldImageView) {
        if (firstTime){
            firstTime = false;
            shieldTextView.setText("0");
            shieldImageView.setImageResource(R.drawable.shield_fill_animation);
            AnimationDrawable rechargeAnimation = (AnimationDrawable) shieldImageView.getDrawable();
            rechargeAnimation.start();

            Handler animationHandler = new Handler();
            animationHandler.postDelayed(new Runnable() {

                public void run() {
                    if (!(shield.getShieldState() instanceof ReadyShieldState) && Player.getInstance().getRealHealth() != 0) {
                        FPSActivity.playShieldChargedSound();

                        shield.setShieldState(new ReadyShieldState(), shieldTextView, shieldImageView);
                    }
                }
            }, shield.rechargeTime);
        }
    }
}
