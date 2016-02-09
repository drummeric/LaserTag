package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
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
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;

public class GameLobbyFragment extends Fragment {

    private final String TAG = "GameLobbyFragment";

    private OnFragmentInteractionListener mListener;
    private Game mGame;
    private Firebase mGameReference;

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

    public void setGame(Game game, final Firebase reference){
        this.mGame = game;
        this.mGameReference = reference;
        game.enableListeners(mGameReference);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGame = dataSnapshot.getValue(Game.class);
                Log.i(TAG, "Game with the following key updated: " + reference.getKey());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Game with the following key failed to update: " + reference.getKey(), firebaseError.toException());
            }
        });
    }

    public Game getGame(){
        return mGame;
    }

    public Firebase getGameReference(){
        return mGameReference;
    }

    private void init(final View view){
        final TextView gameInfo = (TextView) view.findViewById(R.id.text_game_info);
        gameInfo.setText(mGame.toString());

        Button createTeamButton = (Button) view.findViewById(R.id.button_create_team);
        createTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateTeamDialogFragment dialog = new CreateTeamDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(), "create_team_dialog_fragment");
            }
        });

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_team);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        //todo use Map.Entry<K,V> instead of Object
        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Object, TeamViewHolder>(Object.class, R.layout.card_team, TeamViewHolder.class, mGameReference.child("fullKeys")) {

            @Override
            public void populateViewHolder(final TeamViewHolder holder, Object team, int position) {
                final String teamReference = getRef(position).getKey();

                holder.teamName.setText(teamReference.split(":~")[0]);

                if (mGame.findPlayer(LaserTagApplication.globalPlayer).equals(teamReference)){
                    holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                } else {
                    holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                }

                holder.joinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.joinButton.getText().equals(getString(R.string.game_lobby_button_join_team))) {
                            if (mGame.addGlobalPlayer(teamReference, mGameReference)){
                                holder.joinButton.setText(getString(R.string.game_lobby_button_leave_team));
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "This team is full", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mGame.removeGlobalPlayer(teamReference, mGameReference);
                            holder.joinButton.setText(getString(R.string.game_lobby_button_join_team));
                        }
                    }
                });

                holder.playersListView.setAdapter(new FirebaseListAdapter<String>(getActivity(), String.class, R.layout.list_item_player, mGameReference.child("fullKeys").child(teamReference)) {
                    @Override
                    protected void populateView(View view, String player, int position) {
                        ((TextView) view.findViewById(R.id.text_player_name)).setText(player.split(":~")[0]);
                        setListViewHeightBasedOnItems(holder.playersListView);
                    }
                });

                ((FirebaseListAdapter) holder.playersListView.getAdapter()).notifyDataSetChanged();
                setListViewHeightBasedOnItems(holder.playersListView);
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
