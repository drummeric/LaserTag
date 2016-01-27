package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.player.Player;

import java.util.List;

public class PlayerAdapter extends ArrayAdapter<Player> {

    public PlayerAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void setPlayers(List<Player> players) {
        addAll(players);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_player, null);
        }

        Player p = getItem(position);

        if (p != null) {
            TextView playerName = (TextView) v.findViewById(R.id.text_player_name);

            if (playerName != null) {
                playerName.setText(p.getName());
            }
        }

        return v;
    }

}