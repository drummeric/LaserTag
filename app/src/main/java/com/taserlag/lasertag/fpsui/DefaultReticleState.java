package com.taserlag.lasertag.fpsui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.taserlag.lasertag.R;

public class DefaultReticleState extends ReticleState{

    private static Animation mNameAnimation;
    private static Animation mSkullAnimation;

    @Override
    public void updateUI(Reticle reticle) {
        init(reticle);

        if (reticle.getHitPlayerText().getVisibility() == View.VISIBLE) {
            reticle.getHitPlayerText().startAnimation(mNameAnimation);
        }

        if (reticle.getSkullImage().getVisibility() == View.VISIBLE) {
            reticle.getSkullImage().startAnimation(mSkullAnimation);
        }
        reticle.getReticle().setImageResource(R.drawable.reticle1);
    }

    private void init(final Reticle reticle) {
        if (mNameAnimation == null) {
            mNameAnimation = AnimationUtils.loadAnimation(reticle.getContext(), R.anim.fadeout);
            mNameAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    reticle.getHitPlayerText().setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        if (mSkullAnimation == null) {
            mSkullAnimation = AnimationUtils.loadAnimation(reticle.getContext(), R.anim.fadeout);
            mSkullAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    reticle.getSkullImage().setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }
}
