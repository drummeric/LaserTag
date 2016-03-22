package com.taserlag.lasertag.fpsui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.map.ResizeAnimation;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Scoreboard{

    private final int SCOREBOARD_ANIMATION_DURATION = 250;
    private final int SCOREBOARD_MARGIN = 10;
    private final int SCOREBOARD_EXPANDED_WIDTH = 205;

    private int mScoreboardWidth;
    private int mScoreboardHeight;
    private int mScoreboardExpandedWidth;
    private int mScoreboardExpandedHeight;

    private boolean expanded = false;

    private Query mTeamQuery;
    private ValueEventListener mTeamListener;
    private FirebaseRecyclerAdapter mExpandedScoreboardAdapter;

    private View mScoreboard;
    private View playerStatus;
    private View collapsedScoreboard;
    private View expandedScoreboard;
    private Button minimizeButton;

    private FPSActivity mFPS;

    public Scoreboard(final View view, FPSActivity FPS) {
        mFPS = FPS;

        playerStatus = view.findViewById(R.id.layout_fps_player_status);
        collapsedScoreboard = view.findViewById(R.id.table_scoreboard_teams);
        expandedScoreboard = view.findViewById(R.id.layout_scoreboard_expanded);

        calculateScoreboardDimensions(view);
        initOnClicks(view);
        initCollapsedScoreboard(view);
        initExpandedScoreboard(view);
    }

    private void calculateScoreboardDimensions(View view) {
        mScoreboard = view.findViewById(R.id.layout_hud_scoreboard);

        // Needed this, because you must get current width and height AFTER it is drawn on screen
        mScoreboard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mScoreboardWidth = mScoreboard.getMeasuredWidth();
                mScoreboardHeight = mScoreboard.getMeasuredHeight();

                Resources r = mFPS.getResources();
                DisplayMetrics metrics = new DisplayMetrics();
                mFPS.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

                float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SCOREBOARD_MARGIN, r.getDisplayMetrics());
                float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SCOREBOARD_EXPANDED_WIDTH, r.getDisplayMetrics());
                mScoreboardExpandedWidth = Math.round(width);
                mScoreboardExpandedHeight = Math.round(metrics.heightPixels - 2 * margin);
                mScoreboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void initOnClicks(View view){
        minimizeButton = (Button) view.findViewById(R.id.button_scoreboard_minimize);

        mScoreboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandScoreboard();
            }
        });

        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseScoreboard();
            }
        });
    }

    private void initCollapsedScoreboard(View view){
        final TextView topTeamName = (TextView) view.findViewById(R.id.text_view_fps_top_name);
        final TextView topScore = (TextView) view.findViewById(R.id.text_view_fps_top_score);
        final TextView botTeamName = (TextView) view.findViewById(R.id.text_view_fps_bot_name);
        final TextView botScore = (TextView) view.findViewById(R.id.text_view_fps_bot_score);

        mTeamListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot querySnapshot) {
                Iterator<DataSnapshot> iterator = querySnapshot.getChildren().iterator();

                if (querySnapshot.getChildrenCount() == 2) {
                    DBTeam botTeam = iterator.next().getValue(DBTeam.class);
                    DBTeam topTeam = iterator.next().getValue(DBTeam.class);

                    if (!topTeam.getName().equals(Team.getInstance().getName())) {
                        botTeam = Team.getInstance().getDBTeam();
                        topTeamName.setTypeface(null, Typeface.NORMAL);
                        topScore.setTypeface(null, Typeface.NORMAL);
                        botTeamName.setTypeface(null, Typeface.BOLD_ITALIC);
                        botScore.setTypeface(null, Typeface.BOLD_ITALIC);
                    } else {
                        topTeamName.setTypeface(null, Typeface.BOLD_ITALIC);
                        topScore.setTypeface(null, Typeface.BOLD_ITALIC);
                        botTeamName.setTypeface(null, Typeface.NORMAL);
                        botScore.setTypeface(null, Typeface.NORMAL);
                    }
                    topTeamName.setText(topTeam.getName());
                    topScore.setText(String.valueOf(topTeam.getScore()));
                    botTeamName.setText(botTeam.getName());
                    botScore.setText(String.valueOf(botTeam.getScore()));
                } else {
                    topTeamName.setTypeface(null, Typeface.BOLD_ITALIC);
                    topScore.setTypeface(null, Typeface.BOLD_ITALIC);
                    botTeamName.setTypeface(null, Typeface.NORMAL);
                    botScore.setTypeface(null, Typeface.NORMAL);

                    botTeamName.setVisibility(View.GONE);
                    botScore.setVisibility(View.GONE);
                    topTeamName.setText(Team.getInstance().getName());
                    topScore.setText(String.valueOf(Team.getInstance().getScore()));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        mTeamQuery = Game.getInstance().getReference().child("teams").orderByChild("score").limitToLast(2);
        mTeamQuery.addValueEventListener(mTeamListener);
    }

    private void initExpandedScoreboard(View view){
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_fps_scoreboard);
        mExpandedScoreboardAdapter = new FirebaseRecyclerAdapter<DBTeam, TeamScoreboardViewHolder>(DBTeam.class, R.layout.card_team_scoreboard, TeamScoreboardViewHolder.class, Game.getInstance().getReference().child("teams").orderByChild("score")) {

            @Override
            public void populateViewHolder(final TeamScoreboardViewHolder holder, final DBTeam dbTeam, final int position) {
                if (expanded) {

                    holder.teamName.setText(dbTeam.getName());
                    holder.teamScore.setText(String.valueOf(dbTeam.getScore()));

                    if (Team.getInstance().getName().equals(dbTeam.getName())){
                        holder.teamName.setTypeface(null, Typeface.BOLD_ITALIC);
                        holder.teamScore.setTypeface(null, Typeface.BOLD_ITALIC);

                    } else {
                        holder.teamName.setTypeface(null, Typeface.NORMAL);
                        holder.teamScore.setTypeface(null, Typeface.NORMAL);

                    }

                    ArrayList<DBPlayer> players = new ArrayList<>(dbTeam.getPlayers().values());
                    Collections.sort(players);
                    holder.playerListView.setAdapter(new PlayerScoreAdapter(mFPS, players));
                    setListViewHeightBasedOnItems(holder.playerListView);
                }
            }
        };
        LinearLayoutManager manager = new LinearLayoutManager(mFPS);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(mExpandedScoreboardAdapter);
    }

    private void expandScoreboard(){
        if (!expanded) {
            ResizeAnimation anim = new ResizeAnimation(mScoreboard, mScoreboardExpandedWidth, mScoreboardExpandedHeight);
            anim.setDuration(SCOREBOARD_ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    playerStatus.setVisibility(View.GONE);
                    collapsedScoreboard.setVisibility(View.GONE);
                    expandedScoreboard.setVisibility(View.VISIBLE);
                    expanded = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mExpandedScoreboardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mScoreboard.startAnimation(anim);
        }
    }

    private void collapseScoreboard(){
        if (expanded) {
            ResizeAnimation anim = new ResizeAnimation(mScoreboard, mScoreboardWidth, mScoreboardHeight);
            anim.setDuration(SCOREBOARD_ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    expandedScoreboard.setVisibility(View.GONE);
                    collapsedScoreboard.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    playerStatus.setVisibility(View.VISIBLE);
                    expanded = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mScoreboard.startAnimation(anim);
        }
    }

    public void cleanup(){
        mTeamQuery.removeEventListener(mTeamListener);
        mExpandedScoreboardAdapter.cleanup();
    }

    public void endGame(){
        minimizeButton.setText("EXIT GAME");
        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Game.getInstance().getHost().equals(Player.getInstance().getName())) {
                    Game.getInstance().endGame();
                }
                Toast.makeText(LaserTagApplication.getAppContext(), "Thanks for playing!", Toast.LENGTH_SHORT).show();
                mFPS.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Intent intent = new Intent(mFPS, MenuActivity.class);
                mFPS.startActivity(intent);
                mFPS.finish();
            }
        });

        expandScoreboard();

        //make sure scoreboard no longer updates: game about to get deleted
        cleanup();
    }

    //returns semi transparent form of passed color
    private int getIntFromColor(int[] color){
        int red = (color[1] << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int green = (color[2] << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int blue = color[3] & 0x000000FF; //Mask out anything not blue.

        return 0x88000000 | red | green | blue; //0x88000000 for 50% Alpha. Bitwise OR everything together.
    }

    private void setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, mFPS.getResources().getDisplayMetrics());
            float totalItemsHeight = size * numberOfItems;

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (int) totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }

    public static class TeamScoreboardViewHolder extends RecyclerView.ViewHolder {
        TextView teamName;
        TextView teamScore;
        ListView playerListView;

        public TeamScoreboardViewHolder(View itemView) {
            super(itemView);
            teamName = (TextView) itemView.findViewById(R.id.text_team_scoreboard_name);
            teamScore = (TextView) itemView.findViewById(R.id.text_team_scoreboard_score);
            playerListView = (ListView) itemView.findViewById(R.id.list_view_scoreboard_players);
        }
    }

    public class PlayerScoreAdapter extends ArrayAdapter<DBPlayer> {

        public PlayerScoreAdapter(Context context, ArrayList<DBPlayer> players) {
            super(context, 0, players);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // Get the data item for this position
            final DBPlayer dbPlayer = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            PlayerScoreboardViewHolder viewHolder; // view lookup cache stored in tag
            if (view == null) {
                viewHolder = new PlayerScoreboardViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.list_item_player_scoreboard, parent, false);
                viewHolder.playerName = (TextView) view.findViewById(R.id.text_player_scoreboard_name);
                viewHolder.playerScore = (TextView) view.findViewById(R.id.text_player_scoreboard_score);
                viewHolder.playerKD = (TextView) view.findViewById(R.id.text_player_scoreboard_kd);
                viewHolder.layout = (LinearLayout) view.findViewById(R.id.layout_player_scoreboard);
                view.setTag(viewHolder);
            } else {
                viewHolder = (PlayerScoreboardViewHolder) view.getTag();
            }

            viewHolder.playerName.setText(dbPlayer.getName());
            viewHolder.playerScore.setText(String.valueOf(dbPlayer.getPlayerStats().getScore()));
            viewHolder.playerKD.setText(dbPlayer.getPlayerStats().getKills() + " / " + dbPlayer.getPlayerStats().getDeaths());
            viewHolder.layout.setBackgroundColor(getIntFromColor(dbPlayer.getPlayerStats().getColor()));

            if (Player.getInstance().getName().equals(dbPlayer.getName())){
                viewHolder.playerName.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.playerScore.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.playerKD.setTypeface(null, Typeface.BOLD_ITALIC);
            } else {
                viewHolder.playerName.setTypeface(null, Typeface.NORMAL);
                viewHolder.playerScore.setTypeface(null, Typeface.NORMAL);
                viewHolder.playerKD.setTypeface(null, Typeface.NORMAL);
            }
            return view;
        }

        private class PlayerScoreboardViewHolder {
            TextView playerName;
            TextView playerScore;
            TextView playerKD;
            LinearLayout layout;
        }
    }
}