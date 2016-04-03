package com.taserlag.lasertag.fragments;

import android.widget.ArrayAdapter;
import android.widget.StickyButton;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameFollower;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;

public class GameLobbyFragment extends Fragment implements GameFollower {

    private final String TAG = "GameLobbyFragment";

    private FirebaseRecyclerAdapter mAdapter;
    private boolean FPSStarted = false;

    public GameLobbyFragment() {
        // Required empty public constructor
    }

    public static GameLobbyFragment newInstance() {
        GameLobbyFragment fragment = new GameLobbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_lobby, container, false);        // Inflate the layout for this fragment
        Game.getInstance().registerForUpdates(this);
        init(view);
        Player.reset();

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mAdapter.cleanup();
    }

    @Override
    public void notifyGameUpdated() {
        //no op for now
    }

    @Override
    public void notifyGameLoaded(){
    //no op
    }

    public void notifyGameReady(){
        //only start one instance of FPSActivity
        if (!FPSStarted){
            //if you're on a team when the game starts, reset your health and go to FPSActivity
            FPSStarted = true;
            Game.getInstance().unregisterForUpdates(this);
            if (Team.getInstance().getDBTeam()!=null) {
                ((MenuActivity) getActivity()).launchFPS();
            } else {
                //kicks you out of the lobby if the game starts and you arent on a team
                Game.getInstance().leaveGame();
                Toast.makeText(getContext(), "The game has started and you were not on a team", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .remove(getActivity().getSupportFragmentManager().findFragmentByTag("game_lobby_fragment"))
                        .commit();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void notifyGameOver(){
        //no op
    }

    @Override
    public void notifyGameDeleted(){
        //Game has been deleted because the host left the lobby
        Game.getInstance().leaveGame();
        Game.getInstance().unregisterForUpdates(GameLobbyFragment.this);
        Toast.makeText(LaserTagApplication.getAppContext(), "The game host has left the lobby!", Toast.LENGTH_SHORT).show();
        getActivity().getSupportFragmentManager().popBackStack(MenuActivity.MenuFragId, 0);
    }

    @Override
    public void notifyGameScoreUpdated() {
        //will not happen in this screen
    }

    private void init(final View view){
        if (view != null && Game.getInstance().getDBGame() != null) {
            initBackButton(view);

            //set game description at top of screen
            final TextView gameInfo = (TextView) view.findViewById(R.id.text_game_info);
            gameInfo.setText(Game.getInstance().toString());

            initCreateTeamButton(view);

            initReadyButton(view);

            initRecyclerView(view);
        }
    }

    private void initBackButton(View view){
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Game.getInstance().unregisterForUpdates(GameLobbyFragment.this);
                    if (Game.getInstance().getReference()!= null && Game.getInstance().getHost().equals(Player.getInstance().getName())){
                        Game.getInstance().deleteGame();
                    } else if (Team.getInstance().getDBTeam()!=null){
                        Team.getInstance().removeDBPlayer();
                    }

                    Game.getInstance().leaveGame();
                    getActivity().getSupportFragmentManager().popBackStack(MenuActivity.MenuFragId, 0);
                }
                return true;
            }
        });
    }

    private void initCreateTeamButton(View view){
        Button createTeamButton = (Button) view.findViewById(R.id.button_create_team);
        createTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateTeamDialogFragment dialog = new CreateTeamDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(), "create_team_dialog_fragment");
            }
        });
    }

    private void initReadyButton(View view){
        final StickyButton readyButton = (StickyButton) view.findViewById(R.id.button_ready_up);
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if I'm on a team
                if (Team.getInstance().getDBTeam()!=null) {
                    if (readyButton.isStuck()){
                        readyButton.reset();
                        Player.getInstance().resetReady();
                    } else {
                        readyButton.setPressed();
                        Player.getInstance().readyUp();
                    }
                }
            }
        });

    }

    private void initRecyclerView(View view){
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_team);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FirebaseRecyclerAdapter<DBTeam, TeamViewHolder>(DBTeam.class, R.layout.card_team, TeamViewHolder.class, Game.getInstance().getReference().child("teams")) {

            @Override
            public void populateViewHolder(final TeamViewHolder holder, final DBTeam dbTeam, final int position) {
                holder.teamName.setText(dbTeam.getName());

                if (dbTeam.getPlayers().containsKey(Player.getInstance().getName())){
                    holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                } else {
                    holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                }

                holder.joinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.joinButton.getText().equals(getString(R.string.game_lobby_button_join_team))) {
                            if (dbTeam.addDBPlayer(dbTeam.getName())){
                                holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "This team is full", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            dbTeam.removeDBPlayer(dbTeam.getName());
                            holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                        }
                    }
                });

                holder.playersListView.setAdapter(new PlayersAdapter(GameLobbyFragment.this.getContext(), new ArrayList<DBPlayer>(dbTeam.getPlayers().values()), dbTeam));

                setListViewHeightBasedOnItems(holder.playersListView);
            }
        };
        recycler.setAdapter(mAdapter);
    }

    private void setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 31, getActivity().getResources().getDisplayMetrics());
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

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName;
        Button joinButton;
        ListView playersListView;

        public TeamViewHolder(View itemView) {
            super(itemView);
            teamName = (TextView) itemView.findViewById(R.id.text_team_name);
            playersListView = (ListView) itemView.findViewById(R.id.list_view_team);
            joinButton = (Button) itemView.findViewById(R.id.button_join_or_leave_team);
        }
    }

    public class PlayersAdapter extends ArrayAdapter<DBPlayer> {
        private DBTeam dbTeam;

        public PlayersAdapter(Context context, ArrayList<DBPlayer> players, DBTeam dbTeam) {
            super(context, 0, players);
            this.dbTeam = dbTeam;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // Get the data item for this position
            final DBPlayer dbPlayer = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            PlayerViewHolder viewHolder; // view lookup cache stored in tag
            if (view == null) {
                viewHolder = new PlayerViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.list_item_player, parent, false);
                viewHolder.playerName = ((TextView) view.findViewById(R.id.text_player_name));
                viewHolder.colorButton = (Button) view.findViewById(R.id.button_game_lobby_set_player_color);
                viewHolder.playerReady = ((TextView) view.findViewById(R.id.text_player_ready));
                view.setTag(viewHolder);
            } else {
                viewHolder = (PlayerViewHolder) view.getTag();
            }

            viewHolder.playerName.setText(dbPlayer.getName());
            float[] color = dbPlayer.getPlayerStats().getColor();
            viewHolder.colorButton.setBackgroundColor(Color.HSVToColor(color));

            if (dbPlayer.isReady()) {
                viewHolder.playerReady.setVisibility(View.VISIBLE);
            } else {
                viewHolder.playerReady.setVisibility(View.INVISIBLE);
            }

            viewHolder.colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Player.getInstance().resetReady();
                    SetPlayerColorFragment fragment = SetPlayerColorFragment.newInstance(dbPlayer.getName(), dbTeam.getName());
                    ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, fragment, "set_player_color_fragment");
                }
            });

            return view;
        }

        private class PlayerViewHolder {
            TextView playerName;
            Button colorButton;
            TextView playerReady;
        }
    }
}
