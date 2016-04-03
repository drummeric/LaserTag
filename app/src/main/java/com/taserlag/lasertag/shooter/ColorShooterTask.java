package com.taserlag.lasertag.shooter;

import android.graphics.Color;
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

    private final float H_TOLERANCE = 13;

    private final float SV_MIN = 0.15f;
    private final float S_WHITE = .15f;
    private final float V_BLACK = 0.15f;
    private final float V_GRAYMAX = .75f;
    private final float V_GRAYMIN = .25f;


    //{h,s,v, shotCount}
    private static Map<String, float[]> playerColors;

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
        if (isCancelled() || cameraData == null){
            return "";
        }

        int[] hitColor = CameraHelper.getInstance().getTargetColor(cameraData);
        float[] hitHSV = new float[3];
        Color.RGBToHSV(hitColor[1], hitColor[2], hitColor[3], hitHSV);

        Log.i(TAG, "Shot hsv: " + hitHSV[0] + " " + hitHSV[1] + " " + hitHSV[2] + ".");
        return checkColors(hitHSV);
    }

    // returns "TeamName"+":~"+"PlayerName" with the closest color within tolerance or ""
    private String checkColors(float[] hitHSV){
        String smallestTeamPlayer = "";
        float smallestDiff = H_TOLERANCE;
        float currentDiff;
        TeamIterator<DBPlayer> iterator = Game.getInstance().makeIterator();
        while (iterator.hasNext()){
            DBPlayer dbPlayer = iterator.next();
            String playerKey = iterator.currentTeam() + ":~" + dbPlayer.getName();

            //if not me
            if (!dbPlayer.getName().equals(Player.getInstance().getName())) {
                //if friendly fire is on or they aren't on my team
                if (!(Team.getInstance().getName().equals(iterator.currentTeam()) && !Game.getInstance().getFriendlyFire())) {
                    float[] colors = playerColors.get(playerKey);

                    //first time hitting player
                    if (colors == null){
                        colors = new float[4];
                        for (int i = 0; i < colors.length - 1; i++){
                            colors[i] = dbPlayer.getPlayerStats().getColor()[i];
                        }
                        // give initial measurement some weight
                        colors[3] = INITWEIGHT;
                        playerColors.put(playerKey, colors);
                    }

                    //calc color difference
                    currentDiff = checkPlayerColors(hitHSV, colors);

                    //current player has smaller color distance from hitColor
                    if (currentDiff <= smallestDiff) {
                        smallestTeamPlayer = playerKey;
                        smallestDiff = currentDiff;
                    }
                }
            }
        }
        if (!smallestTeamPlayer.equals("")) {
            float[] colorArray = playerColors.get(smallestTeamPlayer);

            //length = 4, only want to loop through first 3 positions (i = 0-2)
            for (int i = 0; i < colorArray.length - 1; i++) {
                colorArray[i] = ((colorArray[i] * colorArray[3]) + hitHSV[i]) / (colorArray[3] + 1);
            }
            colorArray[3]++;

            playerColors.put(smallestTeamPlayer, colorArray);
        }

        return smallestTeamPlayer;
    }

    //returns MAX integer value on no match
    // returns hue difference for player and hit color
    private float checkPlayerColors(float[] hitHSV, float[] playerHSV){
        boolean colorMatch = true;

        if (playerHSV[1]<S_WHITE && playerHSV[2] < V_GRAYMAX && playerHSV[2] > V_GRAYMIN){
            //low sat & med val = gray
            if (hitHSV[1]<S_WHITE && hitHSV[2] < V_GRAYMAX && hitHSV[2] > V_GRAYMIN){
                return 0;
            }
        } else if (playerHSV[1] < S_WHITE && playerHSV[2] > V_GRAYMAX) {
            //high val & low sat = white
            if (hitHSV[1] < S_WHITE && hitHSV[2] > V_GRAYMAX) {
                return 0;
            }
        } else if (playerHSV[2]<V_BLACK){
            //low value = black
            if (hitHSV[2]<V_BLACK){
                return 0;
            }
        } else {
            float colorDiff = Math.abs(hitHSV[0] - playerHSV[0]);

            //check hue within tolerance
            colorMatch &= colorDiff <= H_TOLERANCE;

            //check saturation and value within tolerance
            colorMatch &= hitHSV[1] > SV_MIN;
            colorMatch &= hitHSV[2] > SV_MIN;

            if (colorMatch) {
                return colorDiff;
            }
        }

        return Integer.MAX_VALUE;
    }
}
