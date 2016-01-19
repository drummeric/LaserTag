package com.taserlag.lasertag.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.taserlag.lasertag.camera.CameraPreview;
import com.taserlag.lasertag.map.MapAssistant;
import com.taserlag.lasertag.map.MapHandler;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.map.ResizeAnimation;

public class FPSActivity extends AppCompatActivity implements MapHandler {

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView mAmmo;
    private int ammo = 10;
    private MapAssistant mapAss = MapAssistant.getInstance(this);
    // REMOVE MAPEXPANDED
    private boolean mapExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fps);
        mAmmo = (TextView) findViewById(R.id.ammo_text_view);
        mAmmo.setText(Integer.toString(ammo));
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            // Else initialize camera and seekbar
            initializeCameraAndSeekbar();
        }

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            // Else initialize map
            MapAssistant.getInstance(this).initializeMap();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission was granted
                    initializeCameraAndSeekbar();
                } else {
                    // Camera permission was denied
                    // Exit the application
                    System.exit(0);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission was granted
                    MapAssistant.getInstance(this).initializeMap();
                } else {
                    // Location permission was denied
                    // Exit the application
                    System.exit(0);
                }
                return;
            }
        }
    }

    @Override
    public void handleMapClick(LatLng latLng) {
        // UNCOMMENT FOLLOWING TWO LINES
        //Intent intent = new Intent(this, MapActivity.class);
        //startActivity(intent);

        // REMOVE EVERYTHING BELOW
        int width = 0;
        int height = 0;
        if (!mapExpanded) {
            // Get screen metrics
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

            // Convert 10dp to px
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());

            width = Math.round(metrics.widthPixels - 2*px);
            height = Math.round(metrics.heightPixels - 2*px);
            mapExpanded = true;
        } else {
            // Convert 175dp to px
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175, r.getDisplayMetrics());

            width = Math.round(px);
            height = Math.round(px);
            mapExpanded = false;
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        ResizeAnimation anim = new ResizeAnimation(mapFragment.getView(), width, height);
        anim.setDuration(100);
        mapFragment.getView().startAnimation(anim);
    }

    @Override
    public void handleLocChanged(Location location) {
        mapAss.clearGoogleMap();
        mapAss.addMarker(location);
        // REMOVE IF STATEMENT
        if (!mapExpanded)
            mapAss.animateCamera(location);
    }

    private void initializeCameraAndSeekbar() {
        // Create an instance of camera
        mCamera = getCameraInstance();

        // Create preview of camera
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Initialize seekbar
        VerticalSeekBar mSeekBar = (VerticalSeekBar) findViewById(R.id.zoom_seek_bar);
        mSeekBar.setProgress(0);

        Camera.Parameters params = mCamera.getParameters();
        if (params.isZoomSupported()) {
            mSeekBar.setMax(params.getMaxZoom());
        }

        mSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setZoom(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // Attempt to get a Camera instance
        } catch (Exception e) {
            Log.e("LaserTag", "Camera is not available", e); // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.getHolder().removeCallback(mPreview);
        }
    }

    public void setZoom(int zoom) {
        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();

        if (params.isZoomSupported()) {
            // set the focus mode
            params.setZoom(zoom);
            // set Camera parameters
            mCamera.setParameters(params);
        }
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            shoot();
        }
        return true;
    }

    private void shoot() {
        ammo = ammo - 1;
        mAmmo.setText(Integer.toString(ammo));
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.m4a1single);
        mp.start();
    }

} // FPSActivity