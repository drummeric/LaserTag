package com.taserlag.lasertag.fpsui;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;

import java.util.Iterator;

public class Scoreboard{

    private TextView mTopTeamName;
    private TextView mTopScore;
    private TextView mBotTeamName;
    private TextView mBotScore;

    public Scoreboard(View view){
        mTopTeamName = (TextView) view.findViewById(R.id.text_view_fps_top_name);
        mTopScore = (TextView) view.findViewById(R.id.text_view_fps_top_score);
        mBotTeamName = (TextView) view.findViewById(R.id.text_view_fps_bot_name);
        mBotScore = (TextView) view.findViewById(R.id.text_view_fps_bot_score);

        final Query teamQuery = Game.getInstance().getReference().child("teams").orderByChild("score").limitToLast(2);

        teamQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot querySnapshot) {
                Iterator<DataSnapshot> iterator = querySnapshot.getChildren().iterator();

                if (querySnapshot.getChildrenCount() == 2) {
                    DBTeam botTeam = iterator.next().getValue(DBTeam.class);
                    DBTeam topTeam = iterator.next().getValue(DBTeam.class);

                    if (!topTeam.getName().equals(Team.getInstance().getName())){
                        botTeam = Team.getInstance().getDBTeam();
                        mTopTeamName.setTypeface(null, Typeface.NORMAL);
                        mTopScore.setTypeface(null, Typeface.NORMAL);
                        mBotTeamName.setTypeface(null, Typeface.BOLD_ITALIC);
                        mBotScore.setTypeface(null, Typeface.BOLD_ITALIC);
                    } else {
                        mTopTeamName.setTypeface(null, Typeface.BOLD_ITALIC);
                        mTopScore.setTypeface(null, Typeface.BOLD_ITALIC);
                        mBotTeamName.setTypeface(null, Typeface.NORMAL);
                        mBotScore.setTypeface(null, Typeface.NORMAL);
                    }

                    mTopTeamName.setText(topTeam.getName());
                    mTopScore.setText(String.valueOf(topTeam.getScore()));
                    mBotTeamName.setText(botTeam.getName());
                    mBotScore.setText(String.valueOf(botTeam.getScore()));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

}
