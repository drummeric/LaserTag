package com.taserlag.lasertag.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.team.DBTeam;

public class CreateTeamDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_team, null);

        final EditText teamName = (EditText) dialogView.findViewById(R.id.edit_text_create_team_dialog_team_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.create_team_dialog_title)
                .setView(dialogView)
                        .setPositiveButton(R.string.create_team_dialog_create, null)
                        .setNegativeButton(R.string.create_team_dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        // Create the AlertDialog object and return it
        final AlertDialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (teamName.getText().toString().isEmpty()){
                            teamName.setError(getString(R.string.create_team_dialog_empty_name));
                        } else {

                            if (Game.getInstance() != null && Game.getInstance().createTeamWithPlayer(new DBTeam(teamName.getText().toString()))) {
                                d.dismiss();
                            } else {
                                teamName.setError(getString(R.string.create_team_dialog_team_exists));
                            }
                        }
                    }
                });

                b = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                    }
                });
            }
        });

        return d;
    }
}
