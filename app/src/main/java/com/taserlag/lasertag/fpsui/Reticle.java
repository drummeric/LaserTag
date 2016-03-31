package com.taserlag.lasertag.fpsui;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.taserlag.lasertag.R;

public class Reticle {
    private Animation mShrinkFadeOut;
    private Animation mAnimationGrowShrink;
    private Animation mFadeOut;

    private String mPlayerName;
    private ImageView mReticle;
    private TextView mHitPlayerText;
    private ImageView mSkullImage;

    public Reticle(View view, Context context){

        mHitPlayerText = (TextView) view.findViewById(R.id.text_view_fps_hit_name);
        mReticle = (ImageView) view.findViewById(R.id.reticle_image_view);
        mSkullImage = (ImageView) view.findViewById(R.id.image_view_fps_dead_icon);

        mAnimationGrowShrink = AnimationUtils.loadAnimation(context, R.anim.growshrink);
        mAnimationGrowShrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mReticle.setImageResource(R.drawable.redreticle);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mReticle.setImageResource(R.drawable.reticle1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mShrinkFadeOut = AnimationUtils.loadAnimation(context, R.anim.fadeoutandshrink);
        mShrinkFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mHitPlayerText.setText(mPlayerName);
                mHitPlayerText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHitPlayerText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mFadeOut = AnimationUtils.loadAnimation(context, R.anim.fadeout);
        mFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mSkullImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSkullImage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void showHitAnimation(String playerName){
        mPlayerName = playerName;
        mHitPlayerText.startAnimation(mShrinkFadeOut);
        mReticle.startAnimation(mAnimationGrowShrink);
    }

    public void showDeadAnimation(){
        mSkullImage.startAnimation(mFadeOut);
    }
}
