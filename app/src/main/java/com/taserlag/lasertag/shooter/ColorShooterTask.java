package com.taserlag.lasertag.shooter;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.taserlag.lasertag.activity.FPSActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.camera.CameraPreview;

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
        if (!LaserTagApplication.globalPlayer.getActiveWeapon().canShoot()){
            cancel(true);
        } else {
            LaserTagApplication.globalPlayer.getActiveWeapon().fire();
            fpsCallback.updateGUI();

        }
    }

    @Override
    protected String doInBackground(byte[]... preview) {
        byte[] mPreview = preview[0];
        if (isCancelled()){
            return "";
        }

        int[] hitColor = getTargetColor(mPreview);

        Log.i(TAG, "Shot argb: " + hitColor[0] + " " + hitColor[1] + " " + hitColor[2] + " " + hitColor[3] + ".");

        String hitPlayer = checkColors(hitColor, 40);

        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.m4a1single);
        //mp.start();

        return hitPlayer;
    }

    //averages middle 100 pixels
    public int[] getTargetColor(byte[] mCameraData){
        int pixel;
        int[] argb = new int[4];
        for (int i = 0; i<10 ; i++){
            for (int j = 0; j<10; j++){
                // gets pixel at x,y
                pixel = getYUVvalue(mCameraData, CameraPreview.mWidth, CameraPreview.mHeight, (CameraPreview.mWidth-5+i)/2, (CameraPreview.mHeight-5+j)/2);

                // separates int color to rbg color
                argb[0] = (argb[0]*(i+j) + Color.alpha(pixel))/(i+j+1);
                argb[1] = (argb[1]*(i+j) + Color.red(pixel))/(i+j+1);
                argb[2] = (argb[2]*(i+j) + Color.green(pixel))/(i+j+1);
                argb[3] = (argb[3]*(i+j) + Color.blue(pixel))/(i+j+1);
            }
        }
        return argb;
    }

    private int getYUVvalue(byte[] yuv,int width,int height,int x,int y){
        int total=width*height;
        int Y=(0xff&yuv[y*width+x])-16;
        int U=(0xff&yuv[(y/2)*width+(x&~1)+total+1])-128;
        int V=(0xff&yuv[(y/2)*width+(x&~1)+total])-128;
        return this.convertYUVtoRGB(Y, U, V);
    }

    private int convertYUVtoRGB(int y, int u, int v) {
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);
        if (r < 0) r = 0; else if (r > 262143) r = 262143;
        if (g < 0) g = 0; else if (g > 262143) g = 262143;
        if (b < 0) b = 0; else if (b > 262143) b = 262143;

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
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
