package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;

public class GameLobbyFragment extends Fragment {

    private final String TAG = "GameLobbyFragment";

    private OnFragmentInteractionListener mListener;
    private Game game;
    private Firebase mRef;

    public GameLobbyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_lobby, container, false);        // Inflate the layout for this fragment
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void setGame(Game game, Firebase ref){
        this.game = game;
        mRef = ref;
    }

    private void init(final View view){
        final TextView gameInfo = (TextView) view.findViewById(R.id.text_game_info);
        gameInfo.setText(game.toString());

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_team);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Team, TeamViewHolder>(Team.class, R.layout.card_team, TeamViewHolder.class, mRef.child("teams")) {
            @Override
            public void populateViewHolder(final TeamViewHolder holder, Team team, int position) {
                holder.teamName.setText(team.getName());

                holder.team = team;

                if (holder.joinButton.getText().equals(getString(R.string.game_lobby_button_join_team))) {
                    holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                } else {
                    holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                }

                holder.joinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.joinButton.getText().equals(getString(R.string.game_lobby_button_join_team))) {
                            holder.team.addPlayer(LaserTagApplication.globalPlayer);
                            holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                        } else {
                            holder.team.removePlayer(LaserTagApplication.globalPlayer);
                            holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                        }
                    }
                });

                holder.players.setAdapter(new FirebaseListAdapter<Player>(getActivity(), Player.class, R.layout.list_item_player, mRef.child("teams").child(Integer.toString(position)).child("players")) {
                    @Override
                    protected void populateView(View view, Player player, int position) {
                        ((TextView) view.findViewById(R.id.text_player_name)).setText(player.getName());
                        setListViewHeightBasedOnItems(holder.players);
                    }
                });

                ((FirebaseListAdapter) holder.players.getAdapter()).notifyDataSetChanged();
                setListViewHeightBasedOnItems(holder.players);
            }
        };
        recycler.setAdapter(mAdapter);

    }

    public void setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getActivity().getResources().getDisplayMetrics());
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

    public void doCreateTeam(){
        CreateTeamDialogFragment dialog = new CreateTeamDialogFragment();
        dialog.show(getActivity().getSupportFragmentManager(), "create_team_dialog_fragment");
    }

    public boolean addTeam(Team team){
        return game.addTeam(team);
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView teamName;
        Button joinButton;
        ListView players;
        Team team;

        public TeamViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view_team);
            teamName = (TextView) itemView.findViewById(R.id.text_team_name);
            players = (ListView) itemView.findViewById(R.id.list_view_team);

            joinButton = (Button) itemView.findViewById(R.id.button_join_or_leave_team);
        }

    }
}//Fragment
