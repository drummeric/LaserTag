package com.taserlag.lasertag.fragments;

import android.widget.StickyButton;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameFollower;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;

public class GameLobbyFragment extends Fragment implements GameFollower {

    private final String TAG = "GameLobbyFragment";

    private static final String GAME_KEY_PARAM = "gameKey";

    private OnFragmentInteractionListener mListener;
    private boolean FPSStarted = false;

    private boolean firstTime = true;

    public GameLobbyFragment() {
        // Required empty public constructor
    }

    public static GameLobbyFragment newInstance(String gameKey) {
        GameLobbyFragment fragment = new GameLobbyFragment();
        Bundle args = new Bundle();
        args.putString(GAME_KEY_PARAM, gameKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Game.getInstance(LaserTagApplication.firebaseReference.child("games").child(getArguments().getString(GAME_KEY_PARAM)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_lobby, container, false);        // Inflate the layout for this fragment
        firstTime = true;
        Game.getInstance().registerForUpdates(this);
        init(view);
        //reset Player
        Player.getInstance().resetHealthScoreAndReady();
        Player.reset();

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

    @Override
    public void notifyGameUpdated() {
        if (Game.getInstance().getDBGame() != null) {
            if (firstTime) {
                init(getView());
            }

            //only start one instance of FPSActivity
            if (Game.getInstance().isGameReady()&&!FPSStarted){
                //if you're on a team when the game starts, reset your health and go to FPSActivity
                FPSStarted = true;
                Game.getInstance().getReference().child("gameReady").setValue(false);
                Game.getInstance().unregisterForUpdates(GameLobbyFragment.this);
                if (Player.getInstance().isReady()) {
                    LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").child("ready").setValue(false);
                    ((MenuActivity) getActivity()).launchFPS(Game.getInstance().getKey());
                } else {
                    //kicks you out of the lobby if the game starts and you arent on a team
                    Toast.makeText(getContext(), "The game has started and you were not on a team", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .remove(getActivity().getSupportFragmentManager().findFragmentByTag("game_lobby_fragment"))
                            .commit();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        } else { //Game has been deleted because the host left the lobby
            Game.getInstance().unregisterForUpdates(GameLobbyFragment.this);
            Toast.makeText(LaserTagApplication.getAppContext(), "The game host has left the lobby!", Toast.LENGTH_SHORT).show();
            Player.getInstance().resetActiveGameKey();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getActivity().getSupportFragmentManager().findFragmentByTag("game_lobby_fragment"))
                    .commit();
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void init(final View view){
        if (view != null && Game.getInstance().getDBGame() != null) {
            firstTime = false;
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
                    if (Game.getInstance().getHost().equals(Player.getInstance().getName())){
                        Game.getInstance().deleteGame();
                        Player.getInstance().resetActiveGameKey();
                    } else {
                        Game.getInstance().removeGlobalPlayer();
                    }

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .remove(getActivity().getSupportFragmentManager().findFragmentByTag("game_lobby_fragment"))
                            .commit();
                    getActivity().getSupportFragmentManager().popBackStack();
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
        readyButton.setStickyColor(Color.GREEN);
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Game.getInstance().findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid()).equals("")) {
                    if (readyButton.isStuck()){
                        readyButton.reset();
                        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").child("ready").setValue(false);
                    } else {
                        readyButton.setPressed();
                        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").child("ready").setValue(true);

                        Query queryRef = LaserTagApplication.firebaseReference.child("users").orderByChild("player/activeGameKey").equalTo(Game.getInstance().getKey());
                        queryRef.keepSynced(true);
                        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean gameReady = true;

                                for (DataSnapshot userSnaphot : dataSnapshot.getChildren()) {
                                    DBPlayer dbPlayer = userSnaphot.child("player").getValue(DBPlayer.class);
                                    gameReady &= dbPlayer.isReady();
                                }

                                if (gameReady) {
                                    Game.getInstance().setGameReady();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }
                }
            }
        });

    }

    private void initRecyclerView(View view){
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_team);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        //todo use Map.Entry<K,V> instead of Object
        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Object, TeamViewHolder>(Object.class, R.layout.card_team, TeamViewHolder.class, Game.getInstance().getReference().child("fullKeys")) {

            @Override
            public void populateViewHolder(final TeamViewHolder holder, Object team, int position) {
                final String teamReference = getRef(position).getKey();

                holder.teamName.setText(teamReference.split(":~")[0]);

                if (Game.getInstance().findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid()).equals(teamReference)){
                    holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                } else {
                    holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                }

                holder.joinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.joinButton.getText().equals(getString(R.string.game_lobby_button_join_team))) {
                            if (Game.getInstance().addGlobalPlayer(teamReference)){
                                holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "This team is full", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Game.getInstance().removeGlobalPlayer(teamReference);
                            holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                        }
                    }
                });

                holder.playersListView.setAdapter(new FirebaseListAdapter<String>(getActivity(), String.class, R.layout.list_item_player, Game.getInstance().getReference().child("fullKeys").child(teamReference)) {
                    @Override
                    protected void populateView(View view, String playerUID, final int position) {
                        final TextView playerName = ((TextView) view.findViewById(R.id.text_player_name));
                        final Button colorButton = (Button) view.findViewById(R.id.button_game_lobby_set_player_color);
                        final TextView playerReady = ((TextView) view.findViewById(R.id.text_player_ready));

                        LaserTagApplication.firebaseReference.child("users").child(playerUID).child("player").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue(DBPlayer.class) != null) {
                                    DBPlayer dbPlayer = dataSnapshot.getValue(DBPlayer.class);
                                    playerName.setText(dbPlayer.getName());

                                    int[] color = dbPlayer.getColor();
                                    colorButton.setBackgroundColor(Color.argb(color[0], color[1], color[2], color[3]));

                                    if (dbPlayer.isReady()) {
                                        playerReady.setVisibility(View.VISIBLE);
                                    } else {
                                        playerReady.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                        colorButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SetPlayerColorFragment fragment = SetPlayerColorFragment.newInstance(Game.getInstance().getFullKeys().get(teamReference).get(position));
                                ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, fragment, "set_player_color_fragment");
                            }
                        });

                        setListViewHeightBasedOnItems(holder.playersListView);
                    }
                });

                ((FirebaseListAdapter) holder.playersListView.getAdapter()).notifyDataSetChanged();
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
        CardView cv;
        TextView teamName;
        Button joinButton;
        ListView playersListView;

        public TeamViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view_team);
            teamName = (TextView) itemView.findViewById(R.id.text_team_name);
            playersListView = (ListView) itemView.findViewById(R.id.list_view_team);
            joinButton = (Button) itemView.findViewById(R.id.button_join_or_leave_team);
        }
    }
}
