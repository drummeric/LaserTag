package com.taserlag.lasertag.fragments;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.team.Team;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder>{

    private List<Team> teams;
    private PlayerAdapter pa;

    public TeamAdapter(List<Team> teams) {
        this.teams = teams;
    }

    @Override
    public TeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_team, parent, false);
        TeamViewHolder tvh = new TeamViewHolder(v);
        pa = new PlayerAdapter(parent.getContext(), R.layout.list_item_player);
        return tvh;
    }

    @Override
    public void onBindViewHolder(TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.teamName.setText(team.getName());
        pa.setPlayers(team.getPlayers());
        holder.players.setAdapter(pa);
        setListViewHeightBasedOnItems(holder.players);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView teamName;
        Button joinButton;
        ListView players;

        TeamViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view_team);
            teamName = (TextView)itemView.findViewById(R.id.text_team_name);
            joinButton = (Button)itemView.findViewById(R.id.button_join_or_leave_team);
            players = (ListView)itemView.findViewById(R.id.list_view_team);
        }
    }

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

}