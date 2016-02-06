package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.game.Game;

import java.util.List;


public class JoinGameFragment extends Fragment{

    private final String TAG = "JoinGameFragment";

    private OnFragmentInteractionListener mListener;

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
        /* todo get games
        AsyncAppData<Game> games = LaserTagApplication.kinveyClient.appData("games", Game.class);
        games.get(new KinveyListCallback<Game>() {
            @Override
            public void onSuccess (Game[] result){
                Log.v(TAG, "received " + result.length + " games");

                try {
                    RecyclerView rv = (RecyclerView) getView().findViewById(R.id.recycler_view_game);
                    LinearLayoutManager llm = new LinearLayoutManager(getContext());
                    rv.setLayoutManager(llm);
                    GameAdapter ta = new GameAdapter(new ArrayList<>(Arrays.asList(result)));
                    rv.setAdapter(ta);
                } catch (Exception e){
                    Log.i(TAG, "Screen load cancelled",e);
                }
            }

            @Override
            public void onFailure (Throwable error){
                Log.e(TAG, "failed to fetch all", error);

                CharSequence text = getString(R.string.join_game_load_failure);
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                MainMenuFragment mmf = (MainMenuFragment) getActivity().getSupportFragmentManager().findFragmentByTag("main_menu_fragment");
                ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, mmf, "main_menu_fragment");
            }
        });
        */
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

    public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder>{

        private List<Game> games;

        public GameAdapter(List<Game> games){
            this.games = games;
        }

        @Override
        public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_game, parent, false);
            return new GameViewHolder(v);
        }

        @Override
        public void onBindViewHolder(GameViewHolder holder, int position) {
            holder.game = games.get(position);
            holder.gameName.setText(holder.game.getHost() + "'s Game");
            holder.gameDescription.setText(holder.game.toString());
        }

        @Override
        public int getItemCount() {
            return games.size();
        }

        public class GameViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            Game game;
            TextView gameName;
            TextView gameDescription;

            GameViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.card_view_game);
                gameName = (TextView)itemView.findViewById(R.id.text_view_game_name);
                gameDescription = (TextView)itemView.findViewById(R.id.text_view_game_description);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GameLobbyFragment fragment = new GameLobbyFragment();
                        //todo game id?
                        //fragment.setGameID(game.getId());
                        ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, fragment, "game_lobby_fragment");
                    }
                });

            }
        }//Game View Holder

    }//Game Adapter

}
