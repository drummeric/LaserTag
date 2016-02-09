package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.StickyButton;
import android.widget.Switch;

import com.firebase.client.Firebase;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameType;

public class CreateGameFragment extends Fragment {

    private final String TAG = "CreateGameFragment";

    private StickyButton[] gameTypeButtons = new StickyButton[3];

    private GameType gameType = GameType.TDM;

    private OnFragmentInteractionListener mListener;

    public CreateGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        gameTypeButtons[0] = (StickyButton) view.findViewById(R.id.button_tdm);
        gameTypeButtons[0].setText(GameType.TDM.toString());

        gameTypeButtons[1] = (StickyButton) view.findViewById(R.id.button_ffa);
        gameTypeButtons[1].setText(GameType.FFA.toString());

        gameTypeButtons[2] = (StickyButton) view.findViewById(R.id.button_vip);
        gameTypeButtons[2].setText(GameType.VIP.toString());

        TouchListener tl = new TouchListener();

        for(Button b:gameTypeButtons){
            b.setOnTouchListener(tl);
        }

        gameTypeButtons[0].setPressed();
        gameType = GameType.TDM;
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

    private class TouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            StickyButton button = (StickyButton) v;

            for(StickyButton b:gameTypeButtons){
                b.reset();
            }
            button.setPressed();

            Spinner spinner = (Spinner) getView().findViewById(R.id.spinner_team_size);

            if (button.getText().equals(GameType.FFA.toString())){
                spinner.setEnabled(false);
                spinner.setSelection(0); // 1 player team for FFA
            } else {
                spinner.setEnabled(true);
            }

            gameType = GameType.decodeType(button.getText().toString());

            return true;
        }
    }

    public void saveGame() {
        Game game = new Game();
        game.setHost(LaserTagApplication.globalPlayer.getName());
        game.setGameType(gameType);
        game.setScoreEnabled(((Switch) getView().findViewById(R.id.switch_score)).isChecked());
        game.setScore(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_score)).getSelectedItem().toString()));
        game.setTimeEnabled(((Switch) getView().findViewById(R.id.switch_time)).isChecked());
        game.setMinutes(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_time)).getSelectedItem().toString()));
        game.setMaxTeamSize(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_team_size)).getSelectedItem().toString()));
        game.setFriendlyFire(((Switch) getView().findViewById(R.id.switch_friendly_fire)).isChecked());
        game.setPrivateMatch(((Switch) getView().findViewById(R.id.switch_private)).isChecked());

        Firebase ref = LaserTagApplication.firebaseReference.child("games").push();
        ref.setValue(game);

        GameLobbyFragment fragment = new GameLobbyFragment();
        fragment.setGame(game, ref);
        ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, fragment, "game_lobby_fragment");
    }

}
