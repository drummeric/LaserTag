package com.taserlag.lasertag.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.camera.CameraPreview;
import com.taserlag.lasertag.map.MapAssistant;
import com.taserlag.lasertag.map.MapHandler;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.shooter.ColorShooterTask;
import com.taserlag.lasertag.shooter.ShooterCallback;

import java.util.HashMap;
import java.util.Map;

public class FPSActivity extends AppCompatActivity implements MapHandler{

    private final String TAG = "FPSActivity";
    private static final String GAME_REF_PARAM = "gameRef";

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView mAmmo;
    private MapAssistant mapAss = MapAssistant.getInstance(this);
    private Firebase mGameReference;

    public static Map<String, int[]> colorMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fps);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            mGameReference = LaserTagApplication.firebaseReference.child("games/"+extras.getString(GAME_REF_PARAM));
        }

        doStartCountdown();

        mAmmo = (TextView) findViewById(R.id.ammo_text_view);
        updateGUI();
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

    //Clears back stack and finishes activity. Returns to new MenuActivity
    @Override
    public void onBackPressed(){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
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
        if (mapAss.getMapExpanded()) {
            mapAss.minimizeMap();
        } else {
            mapAss.maximizeMap();
        }
    }

    @Override
    public void handleLocChanged(Location location) {
        mapAss.clearGoogleMap();
        mapAss.addMarker(location);
        if (!mapAss.getMapExpanded())
            mapAss.animateCamera(location);
    }

    //Shows countdown dialog once everyone in game has loaded FPSActivity
    private void doStartCountdown(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Game Starting in:");
        alertDialog.setMessage("Waiting on players...");
        alertDialog.setCancelable(false);
        alertDialog.show();

        final CountDownTimer timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setMessage((millisUntilFinished/1000)+" seconds");
            }

            @Override
            public void onFinish() {
                alertDialog.dismiss();
            }
        };

        //Player has loaded FPSActivity, player ready flag set
        LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").child("ready").setValue(true);

        //Get players in game
        //Check to see if everyone has successfully started FPSActivity -> start timer
        final Query queryRef = LaserTagApplication.firebaseReference.child("users").orderByChild("player/activeGameKey").equalTo(mGameReference.getKey());
        queryRef.keepSynced(true);
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean gameReady = true;

                for (DataSnapshot userSnaphot : dataSnapshot.getChildren()) {
                    Player player = userSnaphot.child("player").getValue(Player.class);
                    gameReady &= player.isReady();
                }

                if (gameReady) {
                    timer.start();
                    //timer has started, player ready flag clear
                    LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").child("ready").setValue(false);
                    queryRef.removeEventListener(this);

                    //init colorMap now that the query is finalized
                    for (DataSnapshot userSnaphot : dataSnapshot.getChildren()) {
                        Player player = userSnaphot.child("player").getValue(Player.class);
                        colorMap.put(player.getName(), player.getColor());
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
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

    public void updateGUI() {
        mAmmo.setText(LaserTagApplication.globalPlayer.retrieveActiveWeapon().getCurrentClipAmmo() + "|" + LaserTagApplication.globalPlayer.retrieveActiveWeapon().getExcessAmmo());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ColorShooterTask asyncTask = new ColorShooterTask(new ShooterCallback() {

                @Override
                public void onFinishShoot(String playerHit) {
                    if (!playerHit.equals("")) {
                        Toast.makeText(FPSActivity.this, "You hit " + playerHit, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void updateGUI(){
                    FPSActivity.this.updateGUI();
                }
            });
            asyncTask.execute(mPreview.getCameraData());
        }
        return true;
    }

    public void reloadWeapon(View view) {
        LaserTagApplication.globalPlayer.retrieveActiveWeapon().reload();
        updateGUI();
    }

    public void swapWeapon(View view) {
        LaserTagApplication.globalPlayer.swapWeapon();
        updateGUI();
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

} // FPSActivity