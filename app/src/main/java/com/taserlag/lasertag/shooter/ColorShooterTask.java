package com.taserlag.lasertag.shooter;

import android.os.AsyncTask;
import android.util.Log;

import com.taserlag.lasertag.camera.CameraHelper;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.team.TeamIterator;

import java.util.HashMap;
import java.util.Map;

public class ColorShooterTask extends AsyncTask<byte[], Void, String> {

    protected ShooterCallback fpsCallback;
    private static final String TAG = "ColorShooterTask";
    private final int INITWEIGHT = 5;
    private final int TOLERANCE = 40;

    //{a, r, g, b, shotCount}
    private static Map<String, int[]> playerColors;

    public ColorShooterTask(ShooterCallback fps){
        fpsCallback = fps;
    }

    public static void resetColorMap(){
        playerColors = new HashMap<>();
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
            String playerKey = iterator.currentTeam() + ":~" + dbPlayer.getName();

            //if not me
            if (!dbPlayer.getName().equals(Player.getInstance().getName())) {
                //if friendly fire is on or they aren't on my team
                if (!(Team.getInstance().getName().equals(iterator.currentTeam()) && !Game.getInstance().getFriendlyFire())) {
                    int[] colors = playerColors.get(playerKey);

                    //first time hitting player
                    if (colors == null){
                        colors = new int[5];
                        for (int i = 0; i < colors.length - 1; i++){
                            colors[i] = dbPlayer.getPlayerStats().getColor()[i];
                        }
                        // give initial measurement some weight
                        colors[4] = INITWEIGHT;
                        playerColors.put(playerKey, colors);
                    }

                    //calc color difference
                    totalDiff = checkPlayerColors(hitColor, colors);

                    //current player has smaller color distance from hitColor
                    if (totalDiff <= distance) {
                        smallestTeamPlayer = playerKey;
                        distance = totalDiff;
                    }
                }
            }
        }
        if (!smallestTeamPlayer.equals("")) {
            int[] colorArray = playerColors.get(smallestTeamPlayer);

            //length = 5, only want to loop through first 4 positions (i = 0-3)
            for (int i = 0; i < colorArray.length - 1; i++) {
                colorArray[i] = ((colorArray[i] * colorArray[4]) + hitColor[i]) / (colorArray[4] + 1);
            }
            colorArray[4]++;

            playerColors.put(smallestTeamPlayer, colorArray);
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
