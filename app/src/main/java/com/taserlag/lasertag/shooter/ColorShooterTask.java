package com.taserlag.lasertag.shooter;

import android.os.AsyncTask;
import android.util.Log;

import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.camera.CameraHelper;

import java.util.Map;

public class ColorShooterTask extends AsyncTask<byte[], Void, String> {

    protected ShooterCallback fpsCallback;
    private static final String TAG = "ColorShooterTask";

    public ColorShooterTask(ShooterCallback fps){
        fpsCallback = fps;
    }

    @Override
    protected void onPostExecute(String result) {
        fpsCallback.onFinishShoot(result);
    }

    @Override
    protected void onPreExecute() {
        if (!LaserTagApplication.globalPlayer.retrieveActiveWeapon().canShoot()){
            cancel(true);
        } else {
            LaserTagApplication.globalPlayer.retrieveActiveWeapon().fire();
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

        String hitPlayer = checkColors(hitColor, 40);

        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.m4a1single);
        //mp.start();

        return hitPlayer;
    }

    // returns player with the closest color within tolerance or ""
    private String checkColors(int[] hitColor, int tolerance){
        String smallestPlayer = "";
        int distance = tolerance *3;
        int totalDiff;
        for (Map.Entry<String, int[]> player: FPSActivity.colorMap.entrySet()){
            totalDiff = checkPlayerColors(hitColor, player.getValue(),tolerance);
            if (totalDiff <= distance){
                smallestPlayer = player.getKey();
                distance = totalDiff;
            }
        }
        return smallestPlayer;
    }

    //returns MAX integer value on no match
    // returns total color difference across rgb values for player and hit color
    private int checkPlayerColors(int[] hitColor, int[] playerColor, int tolerance){
        boolean colorMatch = true;
        int colorDiff = 0;
        for (int i = 0; i<hitColor.length; i++){
            colorMatch &= Math.abs(hitColor[i]-playerColor[i]) <= tolerance;
            colorDiff += Math.abs(hitColor[i]-playerColor[i]);
        }
        if (colorMatch) {
            return colorDiff;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
