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
    private boolean mDead;

    private ReticleState mState = new DefaultReticleState();

    private ImageView mReticle;
    private TextView mHitPlayerText;
    private ImageView mSkullImage;

    private Context mContext;

    public Reticle(View view, Context context){
        mContext = context;
        mHitPlayerText = (TextView) view.findViewById(R.id.text_view_fps_hit_name);
        mReticle = (ImageView) view.findViewById(R.id.reticle_image_view);
        mSkullImage = (ImageView) view.findViewById(R.id.image_view_fps_dead_icon);
    }

    public void setPlayerName(String playerName) {
        mPlayerName = playerName;
    }

    public void setState(ReticleState state, String playerName, boolean dead) {
        mPlayerName = playerName;
        mDead = dead;

        boolean update = !(state instanceof TargetReticleState && (mState instanceof TargetReticleState || mState instanceof HitReticleState));

        if (update) {
            transitionState(state);
        }
    }

    public void transitionState(ReticleState state) {
        mState = state;
        mState.updateUI(this);
    }

    public Context getContext() {
        return mContext;
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public boolean isDead() {
        return mDead;
    }

    public ImageView getReticle() {
        return mReticle;
    }

    public TextView getHitPlayerText() {
        return mHitPlayerText;
    }

    public ImageView getSkullImage() {
        return mSkullImage;
    }
}
