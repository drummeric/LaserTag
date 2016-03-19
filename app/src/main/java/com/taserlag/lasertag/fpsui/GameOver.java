package com.taserlag.lasertag.fpsui;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameOver {
    private TextView mTeamTextView;
    private TextView mMVPTextView;
    private View mGameOverView;
    private View mMapView;
    private View mTimeView;
    private View mReticleView;

    private Context mContext;

    public GameOver(View view, Context context){
        mContext = context;

        mTeamTextView = (TextView) view.findViewById(R.id.text_gameover_team);
        mMVPTextView = (TextView) view.findViewById(R.id.text_gameover_mvp);
        mGameOverView = view.findViewById(R.id.layout_hud_gameover);
        mMapView = (View) view.findViewById(R.id.map).getParent();
        mTimeView = view.findViewById(R.id.text_view_fps_game_time);
        mReticleView = view.findViewById(R.id.reticle_image_view);
        mGameOverView.setVisibility(View.GONE);
    }

    public void endGame(){
        mTeamTextView.setText(getWinningTeams());
        mMVPTextView.setText(getMVP());
        mMapView.setVisibility(View.GONE);
        mTimeView.setVisibility(View.GONE);
        mReticleView.setVisibility(View.GONE);

        Animation slideOn = AnimationUtils.loadAnimation(mContext, R.anim.slide_on_from_left);
        mGameOverView.startAnimation(slideOn);
        mGameOverView.setVisibility(View.VISIBLE);
    }

    private String getWinningTeams(){
        StringBuilder result = new StringBuilder();
        result.append("Winner: ");
        List<DBTeam> teams = new ArrayList<>(Game.getInstance().getTeams().values());
        Collections.sort(teams, new Comparator<DBTeam>() {
            @Override
            public int compare(DBTeam lhs, DBTeam rhs) {
                return rhs.getScore() - lhs.getScore();
            }
        });

        int winner = teams.get(0).getScore();
        result.append(teams.get(0).getName());
        for (int i = 1; i < teams.size(); i++){
            if (teams.get(i).getScore()==winner){
                result.append(", " + teams.get(i).getName());
            } else {
                break;
            }
        }

        return result.toString();
    }

    private String getMVP(){
        TeamIterator<DBPlayer> playerIterator = Game.getInstance().makeIterator();
        DBPlayer mvp = new DBPlayer("ERROR");

        //better succeed
        if (playerIterator.hasNext()) {
            mvp = playerIterator.next();
        }

        while (playerIterator.hasNext()){
            DBPlayer current = playerIterator.next();
            if (mvp.compareTo(current) > 0){
                mvp = current;
            }
        }

        return "MVP: " + mvp.getName();
    }
}