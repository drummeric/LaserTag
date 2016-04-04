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

    private Context mContext;

    public Reticle(View view, Context context){
        mContext = context;
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
                mSkullImage.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void showHitAnimation(String playerName){
        mPlayerName = playerName;
        mHitPlayerText.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        mHitPlayerText.startAnimation(mShrinkFadeOut);
        mReticle.startAnimation(mAnimationGrowShrink);
    }

    public void showDeadAnimation(String playerName){
        mSkullImage.startAnimation(mFadeOut);
        mPlayerName = playerName;
        mHitPlayerText.startAnimation(mShrinkFadeOut);
    }

    public void showHitDetected(String playerName) {
        if (mHitPlayerText.getVisibility()!=View.VISIBLE) {
            mPlayerName = playerName;
            mHitPlayerText.setTextColor(mContext.getResources().getColor(R.color.black));
            mHitPlayerText.startAnimation(mShrinkFadeOut);
        }
    }

    public void showDeadDetected(String playerName){
        if (mHitPlayerText.getVisibility()!=View.VISIBLE) {
            mSkullImage.startAnimation(mFadeOut);
            mPlayerName = playerName;
            mHitPlayerText.setTextColor(mContext.getResources().getColor(R.color.black));
            mHitPlayerText.startAnimation(mShrinkFadeOut);
        }
    }
}
