package com.taserlag.lasertag.fpsui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.taserlag.lasertag.R;

public class HitReticleState extends ReticleState{

    private static Animation mReticleAnimation;

    @Override
    public void updateUI(final Reticle reticle) {
        init(reticle);

        reticle.getHitPlayerText().setText(reticle.getPlayerName());
        reticle.getHitPlayerText().setTextColor(reticle.getContext().getResources().getColor(android.R.color.holo_red_light));
        reticle.getHitPlayerText().setVisibility(View.VISIBLE);

        if (reticle.isDead()){
            reticle.getSkullImage().setVisibility(View.VISIBLE);
        } else {
            reticle.getSkullImage().setVisibility(View.GONE);
        }

        reticle.getReticle().clearAnimation();
        reticle.getReticle().startAnimation(mReticleAnimation);
    }

    private void init(final Reticle reticle){
        if (mReticleAnimation == null){
            mReticleAnimation = AnimationUtils.loadAnimation(reticle.getContext(), R.anim.growshrink);
            mReticleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    reticle.getReticle().setImageResource(R.drawable.redreticle);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    reticle.getReticle().setImageResource(R.drawable.reticle1);
                    if (reticle.getPlayerName().equals("")) {
                        reticle.transitionState(new DefaultReticleState());
                    } else {
                        reticle.transitionState(new TargetReticleState());
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }
}
