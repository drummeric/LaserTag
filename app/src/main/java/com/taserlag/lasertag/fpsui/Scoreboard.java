package com.taserlag.lasertag.fpsui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;

import java.util.Iterator;

public class Scoreboard{

    private boolean expanded = false;

    private TextView mTopTeamName;
    private TextView mTopScore;
    private TextView mBotTeamName;
    private TextView mBotScore;

    public Scoreboard(final View view, final Context context) {
        mTopTeamName = (TextView) view.findViewById(R.id.text_view_fps_top_name);
        mTopScore = (TextView) view.findViewById(R.id.text_view_fps_top_score);
        mBotTeamName = (TextView) view.findViewById(R.id.text_view_fps_bot_name);
        mBotScore = (TextView) view.findViewById(R.id.text_view_fps_bot_score);
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_fps_scoreboard);
        Button minimizeButton = (Button) view.findViewById(R.id.button_scoreboard_minimize);

        View scoreboard = view.findViewById(R.id.layout_fps_scoreboard);
        final View playerStatus = view.findViewById(R.id.layout_fps_player_status);
        final View collapsedScoreboard = view.findViewById(R.id.table_scoreboard_teams);
        final View expandedScoreboard = view.findViewById(R.id.layout_scoreboard_expanded);

        scoreboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!expanded) {
                    playerStatus.setVisibility(View.GONE);
                    collapsedScoreboard.setVisibility(View.GONE);
                    expandedScoreboard.setVisibility(View.VISIBLE);
                    expanded = true;
                }
            }
        });

        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expanded) {
                    expandedScoreboard.setVisibility(View.GONE);
                    collapsedScoreboard.setVisibility(View.VISIBLE);
                    playerStatus.setVisibility(View.VISIBLE);
                    expanded = false;
                }
            }
        });

        final Query teamQuery = Game.getInstance().getReference().child("teams").orderByChild("score").limitToLast(2);

        teamQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot querySnapshot) {
                Iterator<DataSnapshot> iterator = querySnapshot.getChildren().iterator();

                if (querySnapshot.getChildrenCount() == 2) {
                    DBTeam botTeam = iterator.next().getValue(DBTeam.class);
                    DBTeam topTeam = iterator.next().getValue(DBTeam.class);

                    if (!topTeam.getName().equals(Team.getInstance().getName())) {
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
                } else {
                    mTopTeamName.setTypeface(null, Typeface.BOLD_ITALIC);
                    mTopScore.setTypeface(null, Typeface.BOLD_ITALIC);
                    mBotTeamName.setTypeface(null, Typeface.NORMAL);
                    mBotScore.setTypeface(null, Typeface.NORMAL);

                    mBotTeamName.setVisibility(View.GONE);
                    mBotScore.setVisibility(View.GONE);
                    mTopTeamName.setText(Team.getInstance().getName());
                    mTopScore.setText(String.valueOf(Team.getInstance().getScore()));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(LaserTagApplication.getAppContext()));
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<DBTeam, TeamScoreboardViewHolder>(DBTeam.class, R.layout.card_team_scoreboard, TeamScoreboardViewHolder.class, Game.getInstance().getReference().child("teams")) {

            @Override
            public void populateViewHolder(final TeamScoreboardViewHolder holder, final DBTeam dbTeam, final int position) {
                holder.teamName.setText(dbTeam.getName());
                holder.teamScore.setText(String.valueOf(dbTeam.getScore()));

                LayoutInflater inflater = LayoutInflater.from(context);

                for(DBPlayer player:dbTeam.getPlayers().values()) {
                    TableRow newRow = (TableRow) inflater.inflate(R.layout.list_item_player_scoreboard, null, false);
                    ((TextView) newRow.findViewById(R.id.text_player_scoreboard_name)).setText(player.getName());
                    ((TextView) newRow.findViewById(R.id.text_player_scoreboard_score)).setText(String.valueOf(player.getPlayerStats().getScore()));
                    ((TextView) newRow.findViewById(R.id.text_player_scoreboard_kd)).setText(player.getPlayerStats().getKills() + " / " + player.getPlayerStats().getDeaths());
                    newRow.setBackgroundColor(getIntFromColor(player.getPlayerStats().getColor()));
                    holder.playersLayout.addView(newRow);
                }
            }
        };
        recycler.setAdapter(adapter);
    }

    //returns semi transparent form of passed color
    public int getIntFromColor(int[] color){
        int red = (color[1] << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int green = (color[2] << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int blue = color[3] & 0x000000FF; //Mask out anything not blue.

        return 0x88000000 | red | green | blue; //0x88000000 for 50% Alpha. Bitwise OR everything together.
    }

    public static class TeamScoreboardViewHolder extends RecyclerView.ViewHolder {
        TextView teamName;
        TextView teamScore;
        TableLayout playersLayout;

        public TeamScoreboardViewHolder(View itemView) {
            super(itemView);
            teamName = (TextView) itemView.findViewById(R.id.text_team_scoreboard_name);
            teamScore = (TextView) itemView.findViewById(R.id.text_team_scoreboard_score);
            playersLayout = (TableLayout) itemView.findViewById(R.id.table_scoreboard_players);
        }
    }
}