package com.taserlag.lasertag.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.StickyButton;
import android.widget.Toast;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.game.DBGame;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameType;
import com.taserlag.lasertag.player.Player;

import java.util.Date;

public class CreateGameFragment extends Fragment{

    private final String TAG = "CreateGameFragment";

    private StickyButton[] gameTypeButtons = new StickyButton[3];

    private GameType gameType = GameType.TDM;

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

        ArrayAdapter teamSizeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_players, R.layout.spinner_item);
        ((Spinner) view.findViewById(R.id.spinner_team_size)).setAdapter(teamSizeAdapter);

        ArrayAdapter scoreAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_scores, R.layout.spinner_item);
        ((Spinner) view.findViewById(R.id.spinner_score)).setAdapter(scoreAdapter);

        ArrayAdapter timeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_times, R.layout.spinner_item);
        ((Spinner) view.findViewById(R.id.spinner_time)).setAdapter(timeAdapter);

        //just in case
        Game.getInstance().leaveGame();

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
                spinner.setBackground(null);
                spinner.setSelection(0); // 1 player team for FFA
            } else {
                spinner.setEnabled(true);
                spinner.setBackground(getResources().getDrawable(R.drawable.rectangle));
            }

            gameType = GameType.decodeType(button.getText().toString());

            return true;
        }
    }

    public void saveGame() {
        if (MenuActivity.mCurrentLocation != null) {
            DBGame dbGame = new DBGame();
            dbGame.setHost(Player.getInstance().getName());
            dbGame.setGameType(gameType);
            dbGame.setScoreEnabled(((SwitchCompat) getView().findViewById(R.id.switch_score)).isChecked());
            dbGame.setEndScore(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_score)).getSelectedItem().toString()));
            dbGame.setTimeEnabled(((SwitchCompat) getView().findViewById(R.id.switch_time)).isChecked());
            dbGame.setEndMinutes(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_time)).getSelectedItem().toString()));
            dbGame.setMaxTeamSize(Integer.parseInt(((Spinner) getView().findViewById(R.id.spinner_team_size)).getSelectedItem().toString()));
            dbGame.setFriendlyFire(((SwitchCompat) getView().findViewById(R.id.switch_friendly_fire)).isChecked());
            dbGame.setDate(new Date());

            //save new game to DB (with push) and start game lobby
            Game.getInstance(dbGame, dbGame.saveNewGame(MenuActivity.mCurrentLocation));
            ((MenuActivity) getActivity()).replaceFragment(R.id.menu_frame, GameLobbyFragment.newInstance(), "game_lobby_fragment");
        } else {
            Toast.makeText(getContext(),"No location detected! Please try again in a few seconds. \nIf the problem persists, check your location settings.",Toast.LENGTH_LONG).show();
        }
    }

}
