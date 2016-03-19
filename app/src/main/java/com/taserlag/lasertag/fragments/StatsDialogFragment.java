package com.taserlag.lasertag.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.DBGame;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.team.DBTeam;

import java.util.ArrayList;
import java.util.Collections;

public class StatsDialogFragment extends DialogFragment {

    private DBGame mDBGame;
    private String mDBGameKey;
    private FirebaseRecyclerAdapter mAdapter;

    public void setDBGame(DBGame dbGame, String dbGameKey) {
        mDBGame = dbGame;
        mDBGameKey = dbGameKey;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_game_stats, null);

        ((TextView) dialogView.findViewById(R.id.text_view_stats_dialog_description)).setText(mDBGame.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mAdapter.cleanup();
                    }
                });
        // Create the AlertDialog object and return it
        final AlertDialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                RecyclerView recycler = (RecyclerView) dialogView.findViewById(R.id.recycler_view_stats_dialog);
                mAdapter = new FirebaseRecyclerAdapter<DBTeam, TeamStatsDialogViewHolder>(DBTeam.class, R.layout.card_team_stats_dialog, TeamStatsDialogViewHolder.class, LaserTagApplication.firebaseReference.child("finishedGames").child(mDBGameKey).child("teams").orderByChild("score")) {

                    @Override
                    public void populateViewHolder(final TeamStatsDialogViewHolder holder, final DBTeam dbTeam, final int position) {
                        holder.teamName.setText(dbTeam.getName());
                        holder.teamScore.setText(String.valueOf(dbTeam.getScore()));
                        ArrayList<DBPlayer> players = new ArrayList<>(dbTeam.getPlayers().values());
                        Collections.sort(players);
                        holder.playerListView.setAdapter(new PlayerStatsDialogAdapter(getContext(), players));
                        setListViewHeightBasedOnItems(holder.playerListView);
                    }
                };
                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                manager.setReverseLayout(true);
                manager.setStackFromEnd(true);
                recycler.setLayoutManager(manager);
                recycler.setAdapter(mAdapter);
            }
        });

        return d;
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
            float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getActivity().getResources().getDisplayMetrics());
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

    public static class TeamStatsDialogViewHolder extends RecyclerView.ViewHolder {
        TextView teamName;
        TextView teamScore;
        ListView playerListView;

        public TeamStatsDialogViewHolder(View itemView) {
            super(itemView);
            teamName = (TextView) itemView.findViewById(R.id.text_team_stats_dialog_name);
            teamScore = (TextView) itemView.findViewById(R.id.text_team_stats_dialog_score);
            playerListView = (ListView) itemView.findViewById(R.id.list_view_stats_dialog_players);
        }
    }

    public class PlayerStatsDialogAdapter extends ArrayAdapter<DBPlayer> {

        public PlayerStatsDialogAdapter(Context context, ArrayList<DBPlayer> players) {
            super(context, 0, players);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // Get the data item for this position
            final DBPlayer dbPlayer = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            PlayerStatsDialogViewHolder viewHolder; // view lookup cache stored in tag
            if (view == null) {
                viewHolder = new PlayerStatsDialogViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.list_item_player_stats_dialog, parent, false);
                viewHolder.playerName = (TextView) view.findViewById(R.id.text_player_stats_dialog_name);
                viewHolder.playerScore = (TextView) view.findViewById(R.id.text_player_stats_dialog_score);
                viewHolder.playerKD = (TextView) view.findViewById(R.id.text_player_stats_dialog_kd);
                viewHolder.playerHP = (TextView) view.findViewById(R.id.text_player_stats_dialog_hp);
                viewHolder.layout = (LinearLayout) view.findViewById(R.id.layout_player_stats_dialog);
                view.setTag(viewHolder);
            } else {
                viewHolder = (PlayerStatsDialogViewHolder) view.getTag();
            }

            viewHolder.playerName.setText(dbPlayer.getName());
            viewHolder.playerScore.setText(String.valueOf(dbPlayer.getPlayerStats().getScore()));
            viewHolder.playerKD.setText(dbPlayer.getPlayerStats().getKills() + " / " + dbPlayer.getPlayerStats().getDeaths());
            viewHolder.playerHP.setText(String.format("%.2f", dbPlayer.getPlayerStats().getHitAccuracy() * 100) + "%");
            viewHolder.layout.setBackgroundColor(getIntFromColor(dbPlayer.getPlayerStats().getColor()));

            return view;
        }

        private class PlayerStatsDialogViewHolder {
            TextView playerName;
            TextView playerScore;
            TextView playerKD;
            TextView playerHP;
            LinearLayout layout;
        }
    }
}