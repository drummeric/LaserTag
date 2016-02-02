package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kinvey.android.AsyncAppData;
import com.kinvey.java.core.KinveyClientCallback;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;

public class GameLobbyFragment extends Fragment {

    private final String TAG = "GameLobbyFragment";

    private OnFragmentInteractionListener mListener;
    private String gameID;
    private Game game;

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
        AsyncAppData<Game> myGame = LaserTagApplication.kinveyClient.appData("games", Game.class);
        myGame.getEntity(gameID, new KinveyClientCallback<Game>() {
            @Override
            public void onSuccess(Game result) {
                Log.v(TAG, "received " + result.getId());
                game = result;
                init();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "failed to fetchByFilterCriteria", error);
            }
        });

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

    public void setGameID(String id){
        gameID = id;
    }

    private void init(){
        final TextView gameInfo = (TextView) getView().findViewById(R.id.text_game_info);
        gameInfo.setText(game.toString());

        RecyclerView rv = (RecyclerView) getView().findViewById(R.id.recycler_view_team);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        TeamAdapter ta = new TeamAdapter();
        rv.setAdapter(ta);
    }

    public void doCreateTeam(){
        CreateTeamDialogFragment dialog = new CreateTeamDialogFragment();
        dialog.show(getActivity().getSupportFragmentManager(), "create_team_dialog_fragment");
    }

    public void addTeam(Team team){
        game.addTeam(team);

        updateRecyler();
    }

    public void updateRecyler(){
        RecyclerView rv = (RecyclerView) getView().findViewById(R.id.recycler_view_team);
        rv.getAdapter().notifyDataSetChanged();
        AsyncAppData<Game> mygame = LaserTagApplication.kinveyClient.appData("games", Game.class);
        mygame.save(game, new KinveyClientCallback<Game>() {
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "failed to save game data", e);
            }

            @Override
            public void onSuccess(Game g) {
                Log.d(TAG, "saved data for game " + g.getId());
            }
        });
    }

    public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder>{

        @Override
        public TeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_team, parent, false);
            return new TeamViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TeamViewHolder holder, int position) {
            holder.team = game.getTeams().get(position);
            holder.teamName.setText(holder.team.getName());
            ((TeamViewHolder.PlayerAdapter) holder.players.getAdapter()).notifyDataSetChanged();
            setListViewHeightBasedOnItems(holder.players);
        }

        @Override
        public int getItemCount() {
            return game.getTeams().size();
        }

        public class TeamViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView teamName;
            Button joinButton;
            ListView players;
            Team team;

            TeamViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.card_view_team);
                teamName = (TextView)itemView.findViewById(R.id.text_team_name);
                players = (ListView)itemView.findViewById(R.id.list_view_team);
                final PlayerAdapter pa = new PlayerAdapter(itemView.getContext(), R.layout.list_item_player);
                players.setAdapter(pa);
                joinButton = (Button)itemView.findViewById(R.id.button_join_or_leave_team);
                // TODO: Add real players based on device/account
                joinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.addPlayer(new Player("TestName"));
                        updateRecyler();
                    }
                });
            }

            public class PlayerAdapter extends ArrayAdapter<Player> {

                public PlayerAdapter(Context context, int resource) {
                    super(context, resource);
                }

                @Override
                public void add(Player player) {
                   team.getPlayers().add(player);
                }

                @Override
                public Player getItem(int position){
                    return team.getPlayers().get(position);
                }

                @Override
                public int getCount(){
                    if( team != null) {
                        return team.getPlayers().size();
                    } else {
                        return 0;
                    }
                }

                @Override
                public int getPosition(Player p){
                    return team.getPlayers().indexOf(p);
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    View v = convertView;

                    if (v == null) {
                        LayoutInflater vi;
                        vi = LayoutInflater.from(getContext());
                        v = vi.inflate(R.layout.list_item_player, null);
                    }

                    Player p = getItem(position);

                    if (p != null) {
                        TextView playerName = (TextView) v.findViewById(R.id.text_player_name);

                        if (playerName != null) {
                            playerName.setText(p.getName());
                        }
                    }

                    return v;
                }

            }//Player Adapter

        }//Team View Holder

        public boolean setListViewHeightBasedOnItems(ListView listView) {

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

    }//Team Adapter

}//Fragment
