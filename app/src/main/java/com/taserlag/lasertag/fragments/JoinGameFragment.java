package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.FirebaseRecyclerAdapter;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.DBGame;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.DBTeam;
import com.taserlag.lasertag.team.Team;


public class JoinGameFragment extends Fragment{

    private final String TAG = "JoinGameFragment";

    private OnFragmentInteractionListener mListener;

    private FirebaseRecyclerAdapter mAdapter;

    public JoinGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_game, container, false);
        //just in case
        Game.getInstance().leaveGame();
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
    public void onDestroyView(){
        super.onDestroyView();
        mAdapter.cleanup();
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

    private void init(View view){

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.recycler_view_game);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FirebaseRecyclerAdapter<DBGame, GameViewHolder>(DBGame.class, R.layout.card_game, GameViewHolder.class, LaserTagApplication.firebaseReference.child("games")) {

            @Override
            public void populateViewHolder(final GameViewHolder holder, final DBGame dbGame, final int position) {
                holder.gameName.setText(dbGame.getHost()+"'s Game");
                holder.gameDescription.setText(dbGame.toString());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Game.getInstance(dbGame, getRef(position));
                        DBTeam foundTeam = Game.getInstance().findPlayer(Player.getInstance().getName());
                        if (Game.getInstance().isGameOver()){
                            Toast.makeText(LaserTagApplication.getAppContext(),"This game is over!", Toast.LENGTH_SHORT).show();
                        } else if (Game.getInstance().isGameReady()){
                            if (foundTeam!=null) {
                                Team.getInstance(foundTeam);
                                Player.getInstance().join();
                                ((MenuActivity) getActivity()).launchFPS();
                            } else {
                                Toast.makeText(LaserTagApplication.getAppContext(),"This game has started already!",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, GameLobbyFragment.newInstance(), "game_lobby_fragment");
                        }
                    }
                });
            }
        };
        recycler.setAdapter(mAdapter);
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView gameName;
        TextView gameDescription;

        public GameViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view_game);
            gameName = (TextView)itemView.findViewById(R.id.text_view_game_name);
            gameDescription = (TextView)itemView.findViewById(R.id.text_view_game_description);
        }
    }

}
