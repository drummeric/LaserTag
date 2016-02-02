package com.taserlag.lasertag.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;

public class CreateTeamDialogFragment extends DialogFragment {

    private Game game;

    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        final EditText teamName = new EditText(getActivity());
        teamName.setHint(R.string.create_team_dialog_hint);

        int horiz = (int) getResources().getDimension(R.dimen.create_game_dialog_horizontal_margin);
        int vert = (int) getResources().getDimension(R.dimen.create_game_dialog_vertical_margin);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.create_team_dialog_title)
                .setView(teamName, horiz, vert, horiz, vert)
                        .setPositiveButton(R.string.create_team_dialog_create, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Team team = new Team (teamName.getText().toString());
                                team.addPlayer(new Player("Name"));
                                ((GameLobbyFragment) getActivity().getSupportFragmentManager().findFragmentByTag("game_lobby_fragment")).addTeam(team);
                            }
                        })
                        .setNegativeButton(R.string.create_team_dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
