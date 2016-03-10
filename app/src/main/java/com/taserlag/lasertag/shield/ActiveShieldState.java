package com.taserlag.lasertag.shield;


import android.widget.ImageView;
import android.widget.TextView;

import com.taserlag.lasertag.R;

public class ActiveShieldState extends ShieldState {
    @Override
    public void updateUI(Shield shield, TextView shieldTextView, ImageView shieldImageView) {
        shieldTextView.setText(String.valueOf(shield.getStrength()));
        // todo replace with path calculation through string concatenation
        switch(shield.getStrength()/10){
            case 10:
                shieldImageView.setImageResource(R.drawable.shield10);
                break;
            case 9:
                shieldImageView.setImageResource(R.drawable.shield9);
                break;
            case 8:
                shieldImageView.setImageResource(R.drawable.shield8);
                break;
            case 7:
                shieldImageView.setImageResource(R.drawable.shield7);
                break;
            case 6:
                shieldImageView.setImageResource(R.drawable.shield6);
                break;
            case 5:
                shieldImageView.setImageResource(R.drawable.shield5);
                break;
            case 4:
                shieldImageView.setImageResource(R.drawable.shield4);
                break;
            case 3:
                shieldImageView.setImageResource(R.drawable.shield3);
                break;
            case 2:
                shieldImageView.setImageResource(R.drawable.shield2);
                break;
            case 1:
                shieldImageView.setImageResource(R.drawable.shield1);
                break;
            default:
                shieldImageView.setImageResource(R.drawable.shield0);
                break;
        }
        if (shield.getStrength()==0){
            shield.setShieldState(new RechargingShieldState(), shieldTextView, shieldImageView);
        }
    }
}
