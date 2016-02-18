package com.taserlag.lasertag.camera;

import android.graphics.Color;

//Do not rely on CameraHelper until CameraPreview has been created to setWidth/Height
public class CameraHelper {

    private static CameraHelper mInstance;
    private int mWidth;
    private int mHeight;

    public static CameraHelper getInstance() {
        if (mInstance == null) {
            mInstance = new CameraHelper();
        }

        return mInstance;
    }

    public void setWidth(int width) {
        mWidth = width;
    }


    public void setHeight(int height) {
        mHeight = height;
    }

    //averages middle 100 pixels
    public int[] getTargetColor(byte[] cameraData){
        int pixel;
        int[] argb = new int[4];
        for (int i = 0; i<10 ; i++){
            for (int j = 0; j<10; j++){
                // gets pixel at x,y
                pixel = getYUVvalue(cameraData, mWidth, mHeight, (mWidth-5+i)/2, (mHeight-5+j)/2);

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
}