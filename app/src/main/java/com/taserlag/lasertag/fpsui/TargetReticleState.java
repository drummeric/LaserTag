package com.taserlag.lasertag.fpsui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.taserlag.lasertag.R;

public class TargetReticleState extends ReticleState{

    private static Animation mSpinAnimation;

    @Override
    public void updateUI(final Reticle reticle) {
        init(reticle);
        reticle.getReticle().startAnimation(mSpinAnimation);
    }

    private void init(final Reticle reticle) {
        if (mSpinAnimation == null) {
            mSpinAnimation = AnimationUtils.loadAnimation(reticle.getContext(), R.anim.spin);

            mSpinAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    reticle.getHitPlayerText().setText(reticle.getPlayerName());
                    reticle.getHitPlayerText().setTextColor(reticle.getContext().getResources().getColor(R.color.black));
                    reticle.getHitPlayerText().setVisibility(View.VISIBLE);
                    if (reticle.isDead()) {
                        reticle.getSkullImage().setVisibility(View.VISIBLE);
                    } else {
                        reticle.getSkullImage().setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (!reticle.getPlayerName().equals("")) {
                        reticle.getHitPlayerText().setText(reticle.getPlayerName());
                        reticle.getHitPlayerText().setTextColor(reticle.getContext().getResources().getColor(R.color.black));
                        reticle.getHitPlayerText().setVisibility(View.VISIBLE);

                        if (reticle.isDead()) {
                            reticle.getSkullImage().setVisibility(View.VISIBLE);
                        } else {
                            reticle.getSkullImage().setVisibility(View.GONE);
                        }
                    } else {
                        reticle.getReticle().clearAnimation();
                        reticle.transitionState(new DefaultReticleState());
                    }
                }
            });
        }
    }
}
