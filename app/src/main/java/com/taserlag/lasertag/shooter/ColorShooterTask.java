package com.taserlag.lasertag.shooter;

import android.os.AsyncTask;
import android.util.Log;

import com.taserlag.lasertag.camera.CameraHelper;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.TeamIterator;

public class ColorShooterTask extends AsyncTask<byte[], Void, String> {

    protected ShooterCallback fpsCallback;
    private static final String TAG = "ColorShooterTask";
    private final int TOLERANCE = 60;

    public ColorShooterTask(ShooterCallback fps){
        fpsCallback = fps;
    }

    @Override
    protected void onPostExecute(String result) {
        fpsCallback.onFinishShoot(result);
    }

    @Override
    protected void onPreExecute() {
        if (!Player.getInstance().retrieveActiveWeapon().fire()){
            cancel(true);
        } else {
            fpsCallback.updateGUI();
        }
    }

    @Override
    protected String doInBackground(byte[]... data) {
        byte[] cameraData = data[0];
        if (isCancelled()){
            return "";
        }

        int[] hitColor = CameraHelper.getInstance().getTargetColor(cameraData);

        Log.i(TAG, "Shot argb: " + hitColor[0] + " " + hitColor[1] + " " + hitColor[2] + " " + hitColor[3] + ".");

        return checkColors(hitColor);
    }

    // returns "TeamName"+":~"+"PlayerName" with the closest color within tolerance or ""
    private String checkColors(int[] hitColor){
        String smallestTeamPlayer = "";
        int distance = TOLERANCE *3;
        int totalDiff;
        TeamIterator<DBPlayer> iterator = Game.getInstance().makeIterator();
        while (iterator.hasNext()){
            DBPlayer dbPlayer = iterator.next();

            if (!dbPlayer.getName().equals(Player.getInstance().getName())) {
                totalDiff = checkPlayerColors(hitColor, dbPlayer.getPlayerStats().getColor());
                if (totalDiff <= distance) {
                    smallestTeamPlayer = iterator.currentTeam() + ":~" + dbPlayer.getName();
                    distance = totalDiff;
                }
            }
        }

        return smallestTeamPlayer;
    }

    //returns MAX integer value on no match
    // returns total color difference across rgb values for player and hit color
    private int checkPlayerColors(int[] hitColor, int[] playerColor){
        boolean colorMatch = true;
        int colorDiff = 0;
        for (int i = 0; i<hitColor.length; i++){
            colorMatch &= Math.abs(hitColor[i]-playerColor[i]) <= TOLERANCE;
            colorDiff += Math.abs(hitColor[i]-playerColor[i]);
        }
        if (colorMatch) {
            return colorDiff;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
