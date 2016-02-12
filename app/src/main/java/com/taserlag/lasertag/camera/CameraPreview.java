package com.taserlag.lasertag.camera;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private byte[] mCameraData;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //averages middle 100 pixels
    public int[] getTargetColor(){
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        final int width = size.width;
        final int height = size.height;
        int pixel;
        int[] argb = new int[4];
        for (int i = 0; i<10 ; i++){
            for (int j = 0; j<10; j++){
                // gets pixel at x,y
                pixel = getYUVvalue(mCameraData, width, height, (width-5+i)/2, (height-5+j)/2);

                // separates int color to rbg color
                argb[0] = (argb[0]*(i+j) + Color.alpha(pixel))/(i+j+1);
                argb[1] = (argb[1]*(i+j) + Color.red(pixel))/(i+j+1);
                argb[2] = (argb[2]*(i+j) + Color.green(pixel))/(i+j+1);
                argb[3] = (argb[3]*(i+j) + Color.blue(pixel))/(i+j+1);
            }
        }
        return argb;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            //Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);

            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mCameraData = data;
                }
            });

            mCamera.startPreview();

        } catch (Exception e){
            //Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
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