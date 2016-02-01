package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;

import java.util.ArrayList;

public class GameLobbyFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String gameID;
    private Game game;

    public GameLobbyFragment() {
        // Required empty public constructor
    }


    public static GameLobbyFragment newInstance(String param1, String param2) {
        return new GameLobbyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_lobby, container, false);        // Inflate the layout for this fragment

        final TextView gameInfo = (TextView) view.findViewById(R.id.text_game_info);
        /*
        ParseQuery<Game> query = ParseQuery.getQuery(Game.class);
        try {
            game = query.get(gameID);
            gameInfo.setText(game.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_view_team);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        // TODO: Remove default teams
        Team team1 = new Team("Team 1");
        Team team2 = new Team("Team 2");
        Team team3 = new Team("Team 3");
        game.addTeam(team1);
        game.addTeam(team2);
        game.addTeam(team3);
        team1.addPlayer(new Player("Player 1"));
        team1.addPlayer(new Player("Player 2"));
        team1.addPlayer(new Player("Player 3"));
        team1.addPlayer(new Player("Player 4"));
        team1.addPlayer(new Player("Player 5"));
        team2.addPlayer(new Player("Player 6"));
        team2.addPlayer(new Player("Player 7"));
        team3.addPlayer(new Player("Player 8"));
        team3.addPlayer(new Player("Player 9"));
        team3.addPlayer(new Player("Player 10"));

        TeamAdapter ta = new TeamAdapter(new ArrayList<>(game.getTeams().values()));
        rv.setAdapter(ta);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void setGameID(String id){
        gameID = id;
    }
}
