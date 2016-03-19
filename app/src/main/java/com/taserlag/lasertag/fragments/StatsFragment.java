package com.taserlag.lasertag.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.DBGame;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class StatsFragment extends Fragment {

    private FirebaseRecyclerAdapter mAdapter;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        init(view);
        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mAdapter.cleanup();
    }

    private void init(View view){
        ((TextView) view.findViewById(R.id.text_view_stats_fragment_title)).setText(Player.getInstance().getName() + "'s Stats");
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_stats_game);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FirebaseRecyclerAdapter<String, GameStatsViewHolder>(String.class, R.layout.card_game_stats, GameStatsViewHolder.class, LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.getUid()).child("previousGames")) {

            @Override
            public void populateViewHolder(final GameStatsViewHolder holder, final String gameKey, final int position) {
                LaserTagApplication.firebaseReference.child("finishedGames").child(gameKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final DBGame dbGame = dataSnapshot.getValue(DBGame.class);
                        holder.gameName.setText(dbGame.getHost() + "'s " + dbGame.getGameType().toString());
                        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
                        holder.gameDate.setText(dateFormat.format(dbGame.getDate()));
                        DBPlayer dbPlayer = dbGame.findPlayer(Player.getInstance().getName()).getPlayers().get(Player.getInstance().getName());
                        holder.playerScore.setText("Score: " + dbPlayer.getPlayerStats().getScore());
                        holder.playerKD.setText("KD: " + dbPlayer.getPlayerStats().getKills() + " / " + dbPlayer.getPlayerStats().getDeaths());
                        holder.playerHP.setText("HP: " + String.format("%.2f", dbPlayer.getPlayerStats().getHitAccuracy() * 100) + "%");
                        holder.playerStatsView.setBackgroundColor(getIntFromColor(dbPlayer.getPlayerStats().getColor()));

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                StatsDialogFragment dialog = new StatsDialogFragment();
                                dialog.setDBGame(dbGame, gameKey);
                                dialog.show(getActivity().getSupportFragmentManager(), "stats_dialog_fragment");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
        };
        recycler.setAdapter(mAdapter);
    }

    //returns semi transparent form of passed color
    private int getIntFromColor(int[] color){
        int red = (color[1] << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int green = (color[2] << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int blue = color[3] & 0x000000FF; //Mask out anything not blue.

        return 0x88000000 | red | green | blue; //0x88000000 for 50% Alpha. Bitwise OR everything together.
    }

    public static class GameStatsViewHolder extends RecyclerView.ViewHolder {
        TextView gameName;
        TextView gameDate;
        TextView playerScore;
        TextView playerKD;
        TextView playerHP;
        View playerStatsView;

        public GameStatsViewHolder(View itemView) {
            super(itemView);
            gameName = (TextView)itemView.findViewById(R.id.text_view_stats_game_name);
            gameDate = (TextView)itemView.findViewById(R.id.text_view_stats_game_date);
            playerScore = (TextView)itemView.findViewById(R.id.text_view_stats_player_score);
            playerKD = (TextView)itemView.findViewById(R.id.text_view_stats_player_kd);
            playerHP = (TextView)itemView.findViewById(R.id.text_view_stats_player_hp);
            playerStatsView = itemView.findViewById(R.id.layout_stats_player);
        }
    }
}
