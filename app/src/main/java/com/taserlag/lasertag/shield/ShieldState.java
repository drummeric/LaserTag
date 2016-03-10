package com.taserlag.lasertag.shield;

import android.widget.ImageView;
import android.widget.TextView;

public abstract class ShieldState {

    public abstract void updateUI(Shield shield, TextView shieldTextView, ImageView shieldImageView);

    public boolean deploy(Shield shield, TextView shieldTextView, ImageView shieldImageView){
        return false;
    }
}
