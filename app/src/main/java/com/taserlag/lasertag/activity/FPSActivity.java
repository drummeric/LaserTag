package com.taserlag.lasertag.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.camera.CameraPreview;
import com.taserlag.lasertag.camera.Zoom;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameFollower;
import com.taserlag.lasertag.map.MapAssistant;
import com.taserlag.lasertag.map.MapHandler;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.player.DBPlayer;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.player.PlayerFollower;
import com.taserlag.lasertag.shooter.ColorShooterTask;
import com.taserlag.lasertag.shooter.ShooterCallback;

import java.util.HashMap;
import java.util.Map;

public class FPSActivity extends AppCompatActivity implements MapHandler, GameFollower, PlayerFollower{

    private final String TAG = "FPSActivity";

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    private final int TIMER_LENGTH_MS = 1000;
    private final int RESPAWN_TIMER_LENGTH_MS = 5000;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Zoom mCameraZoom = Zoom.NONE;

    private TextView mClipAmmoText;
    private TextView mTotalAmmoText;
    private TextView mWeaponText;
    private static TextView mHealthText;
    private static TextView mShieldText;
    private static ImageView mShieldImage;
    private ImageView mGunImage;
    private static ImageView mScreenFlash;
    private TextView mScoreText;
    private TextView mZoomText;

    private MapAssistant mapAss = MapAssistant.getInstance(this);

    private SoundPool mSoundPool;
    private static final int MAX_STREAMS = 1;
    private static final int SOURCE_QUALITY = 0;
    private int mShootSound;

    public static Map<String, DBPlayer> dbPlayerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fps);

        doStartCountdown();

        mScoreText = (TextView) findViewById(R.id.text_view_fps_score);
        mWeaponText = (TextView) findViewById(R.id.text_view_fps_weapon);
        mHealthText = (TextView) findViewById(R.id.text_view_fps_health);
        mShieldText = (TextView) findViewById(R.id.text_view_fps_shield);
        mShieldImage = (ImageView) findViewById(R.id.image_view_fps_shield);
        mGunImage = (ImageView) findViewById(R.id.image_view_fps_gun);
        mScreenFlash = (ImageView) findViewById(R.id.image_view_fps_screen_flash);
        mTotalAmmoText = (TextView) findViewById(R.id.text_view_fps_total_ammo);
        mClipAmmoText = (TextView) findViewById(R.id.text_view_fps_clip_ammo);
        mZoomText = (TextView) findViewById(R.id.text_view_fps_zoom);

        //init UI (health, weapons, ammo, shield)
        resetUIOnRespawn();

        //init score text
        updateScoreText(Player.getInstance().getScore());

        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SOURCE_QUALITY);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            }
        });
        mShootSound = mSoundPool.load(this, R.raw.m4a1single, 1);
        Player.getInstance().registerForUpdates(this);
        Game.getInstance().registerForUpdates(this);
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
            // Else initialize camera
            initializeCamera();
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
    public void notifyPlayerUpdated() {
        updateScoreText(Player.getInstance().getScore());
        if (Player.getInstance().getRealHealth()<=0){
            final AlertDialog alertDialog = new AlertDialog.Builder(FPSActivity.this).create();
            alertDialog.setTitle("You Died!");
            alertDialog.setMessage("Respawning in " + (RESPAWN_TIMER_LENGTH_MS/1000) + " seconds");
            alertDialog.setCancelable(false);
            alertDialog.show();

            new CountDownTimer(RESPAWN_TIMER_LENGTH_MS, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    alertDialog.setMessage("Respawning in "+(millisUntilFinished/1000)+" seconds");
                }

                @Override
                public void onFinish() {
                    alertDialog.dismiss();
                    //reset health, weapons and shields
                    resetUIOnRespawn();
                    hideSystemUI();
                }
            }.start();
        }
    }

    @Override
    public void notifyGameUpdated() {
        if (Game.getInstance().isGameOver()){
            gameOver();
        }
    }

    public void gameOver() {
        Toast.makeText(LaserTagApplication.getAppContext(), "Game over!", Toast.LENGTH_SHORT).show();
    }

    //Shows countdown dialog once everyone in game has loaded FPSActivity
    private void doStartCountdown(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Game Starting in:");
        alertDialog.setMessage("Waiting on players...");
        alertDialog.setCancelable(false);
        alertDialog.show();

        final CountDownTimer timer = new CountDownTimer(TIMER_LENGTH_MS, 1000) {
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
        final Query queryRef = LaserTagApplication.firebaseReference.child("users").orderByChild("player/activeGameKey").equalTo(Game.getInstance().getKey());
        queryRef.keepSynced(true);
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean gameReady = true;

                for (DataSnapshot userSnaphot : dataSnapshot.getChildren()) {
                    DBPlayer dbPlayer = userSnaphot.child("player").getValue(DBPlayer.class);
                    gameReady &= dbPlayer.isReady();
                }

                if (gameReady) {
                    timer.start();
                    //timer has started, player ready flag clear
                    LaserTagApplication.firebaseReference.child("users").child(LaserTagApplication.firebaseReference.getAuth().getUid()).child("player").child("ready").setValue(false);
                    queryRef.removeEventListener(this);

                    //init dbPlayerMap now that the query is finalized
                    for (DataSnapshot userSnaphot : dataSnapshot.getChildren()) {
                        DBPlayer dbPlayer = userSnaphot.child("player").getValue(DBPlayer.class);
                        dbPlayerMap.put(userSnaphot.getKey(), dbPlayer);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void resetUIOnRespawn(){
        //reset health, weapons and shield
        Player.reset();
        updateHealthText();
        updateShieldUI();
        readyShieldImage();
        updateAmmoText();
        updateWeaponText();
    }

// todo replace with path calculation through string concatenation
    private static void updateShieldImage(){
        switch(Player.getInstance().getShield().getStrength()/10){
            case 10:
                mShieldImage.setImageResource(R.drawable.shield10);
                break;
            case 9:
                mShieldImage.setImageResource(R.drawable.shield9);
                break;
            case 8:
                mShieldImage.setImageResource(R.drawable.shield8);
                break;
            case 7:
                mShieldImage.setImageResource(R.drawable.shield7);
                break;
            case 6:
                mShieldImage.setImageResource(R.drawable.shield6);
                break;
            case 5:
                mShieldImage.setImageResource(R.drawable.shield5);
                break;
            case 4:
                mShieldImage.setImageResource(R.drawable.shield4);
                break;
            case 3:
                mShieldImage.setImageResource(R.drawable.shield3);
                break;
            case 2:
                mShieldImage.setImageResource(R.drawable.shield2);
                break;
            case 1:
                mShieldImage.setImageResource(R.drawable.shield1);
                break;
            default:
                mShieldImage.setImageResource(R.drawable.shield0);
                break;
        }
    }

    //10 seconds after charging starts, transition to ready animation
    public static void rechargeShieldImage(){
        mShieldImage.setImageResource(R.drawable.shield_fill_animation);
        AnimationDrawable rechargeAnimation = (AnimationDrawable) mShieldImage.getDrawable();
        rechargeAnimation.start();

        Handler animationHandler = new Handler();
        animationHandler.postDelayed(new Runnable() {

            public void run() {
                readyShieldImage();
            }
        }, 10000);
    }

    // shield ready (used on init)
    private static void readyShieldImage(){
        mShieldImage.setImageResource(R.drawable.shield_ready_animation);
        AnimationDrawable rechargeAnimation = (AnimationDrawable) mShieldImage.getDrawable();
        rechargeAnimation.start();
    }

    private void updateScoreText(int score){
        mScoreText.setText(String.valueOf(score));
    }

    // health values read from singleton Player
    public static  void updateHealthText(){
        mHealthText.setText(String.valueOf(Player.getInstance().getRealHealth()));

        //flash the screen red
        mScreenFlash.setVisibility(View.VISIBLE);
        Handler flashHandler = new Handler();
        flashHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScreenFlash.setVisibility(View.INVISIBLE);
            }
        }, 500);

    }

    // shield values read from singleton Player
    // updates image based on current shield health
    public static void updateShieldUI(){
        mShieldText.setText(String.valueOf(Player.getInstance().getShield().getStrength()));
        updateShieldImage();
    }

    //rotates gun image over 75 ms, plays sound and updates ammo text view
    private void updateGunUI(){
        Animation rotateAnimation = new RotateAnimation(0, -20, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(75);
        rotateAnimation.setRepeatCount(0);
        rotateAnimation.setFillAfter(false);

        mGunImage.startAnimation(rotateAnimation);
        mSoundPool.play(mShootSound, 1, 1, 1, 0, 1);
        updateAmmoText();
    }

    // no param since ammo not stored in database
    private void updateAmmoText() {
        int clip = Player.getInstance().retrieveActiveWeapon().getCurrentClipAmmo();
        if (clip < 10){
            mClipAmmoText.setText("0"+String.valueOf(clip));
        } else {
            mClipAmmoText.setText(String.valueOf(clip));
        }
        mTotalAmmoText.setText("/" + String.valueOf(Player.getInstance().retrieveActiveWeapon().getExcessAmmo()) + " ");
    }

    private void updateWeaponText(){
        mWeaponText.setText("[ "+Player.getInstance().retrieveActiveWeapon().toString()+" ]");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ColorShooterTask asyncTask = new ColorShooterTask(new ShooterCallback() {

                @Override
                public void onFinishShoot(String playerHitUID) {
                    if (!playerHitUID.equals("")) {
                        if (Player.getInstance().decrementHealthAndIncMyScore(Player.getInstance().retrieveActiveWeapon().getStrength(), playerHitUID, Game.getInstance().findPlayer(LaserTagApplication.firebaseReference.getAuth().getUid()).split(":~")[1])){
                            final ImageView reticle = ((ImageView) FPSActivity.this.findViewById(R.id.reticle_image_view));
                            reticle.setImageResource(R.drawable.redreticle);

                            //animation lasts 500 ms total
                            Animation animationGrowShrink = AnimationUtils.loadAnimation(FPSActivity.this, R.anim.growshrink);
                            reticle.startAnimation(animationGrowShrink);

                            Handler flashHandler = new Handler();
                            flashHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    reticle.setImageResource(R.drawable.reticle1);
                                }
                            }, 500);
                        }
                    }
                }

                @Override
                public void updateGUI(){
                    updateGunUI();
                }
            });
            asyncTask.execute(mPreview.getCameraData());
        }
        return true;
    }

    //onClick listener
    public void deployShield(View view){
        if (Player.getInstance().deployShield()) {
            updateShieldUI();
        }
    }

    public void reloadWeapon(View view) {
        Player.getInstance().retrieveActiveWeapon().reload();
        updateAmmoText();
    }

    public void swapWeapon(View view) {
        Player.getInstance().swapWeapon();
        updateAmmoText();
        updateWeaponText();
    }

    public void scopeClick(View view){
        Camera.Parameters params = mCamera.getParameters();
        if (params.isZoomSupported()) {
            switch (mCameraZoom) {
                case NONE:
                    setZoom(params.getMaxZoom()/2);
                    mZoomText.setText("2x");
                    mCameraZoom = Zoom.HALF;
                    break;

                case HALF:
                    setZoom(params.getMaxZoom());
                    mZoomText.setText("4x");
                    mCameraZoom = Zoom.FULL;
                    break;

                case FULL:
                    setZoom(0);
                    mZoomText.setText("");
                    mCameraZoom = Zoom.NONE;
                    break;
            }
        }
    }

    private void initializeCamera() {
        // Create an instance of camera
        mCamera = getCameraInstance();

        // Create preview of camera
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission was granted
                    initializeCamera();
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