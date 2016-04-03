package com.taserlag.lasertag.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
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
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.taserlag.lasertag.camera.CameraPreview;
import com.taserlag.lasertag.camera.Zoom;
import com.taserlag.lasertag.fpsui.GameOver;
import com.taserlag.lasertag.fpsui.Reticle;
import com.taserlag.lasertag.fpsui.Scoreboard;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.game.GameFollower;
import com.taserlag.lasertag.game.GameType;
import com.taserlag.lasertag.map.MapAssistant;
import com.taserlag.lasertag.map.MapHandler;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.player.Player;
import com.taserlag.lasertag.player.PlayerFollower;
import com.taserlag.lasertag.shield.ReadyShieldState;
import com.taserlag.lasertag.shooter.ColorShooterTask;
import com.taserlag.lasertag.shooter.ShooterCallback;
import com.taserlag.lasertag.team.Team;
import com.taserlag.lasertag.team.TeamFollower;

public class FPSActivity extends AppCompatActivity implements MapHandler, GameFollower, TeamFollower, PlayerFollower{

    private final String TAG = "FPSActivity";

    public static final String PREFS_NAME = "LaserTag";
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    private final int TIMER_LENGTH_MS = 3000;
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
    private static ImageView mScreenFlash;
    private TextView mTimeText;
    private TextView mZoomText;
    private ImageView mGunImage;

    private View mHUD;
    private Scoreboard mScoreboard;
    private GameOver mGameOver;
    private Reticle mReticle;

    private AlertDialog mGameLoadingAlertDialog;
    private AlertDialog mVIPRespawnDialog;

    private MapAssistant mapAss = MapAssistant.getInstance(this);

    private SoundPool mSoundPool;
    private static final int MAX_STREAMS = 1;
    private static final int SOURCE_QUALITY = 0;
    private int mShootSound;

    private float touchDownY;
    private boolean hudVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fps);

        // waiting on players countdown
        if (!Game.getInstance().isLoaded()) {
            startLoading();
            //wait for game to load
        } else {
            returnToGame();
        }

        mHUD = findViewById(R.id.layout_fps_hud);
        mTimeText = (TextView) findViewById(R.id.text_view_fps_game_time);
        mWeaponText = (TextView) findViewById(R.id.text_view_fps_weapon);
        mHealthText = (TextView) findViewById(R.id.text_view_fps_health);
        mShieldText = (TextView) findViewById(R.id.text_view_fps_shield);
        mShieldImage = (ImageView) findViewById(R.id.image_view_fps_shield);
        mGunImage = (ImageView) findViewById(R.id.image_view_fps_gun);
        mScreenFlash = (ImageView) findViewById(R.id.image_view_fps_screen_flash);
        mTotalAmmoText = (TextView) findViewById(R.id.text_view_fps_total_ammo);
        mClipAmmoText = (TextView) findViewById(R.id.text_view_fps_clip_ammo);
        mZoomText = (TextView) findViewById(R.id.text_view_fps_zoom);

        //init UI helper objects
        View content = findViewById(android.R.id.content);
        mScoreboard = new Scoreboard(content,this);
        mGameOver = new GameOver(content, this);
        mReticle = new Reticle(content, this);

        //init UI (health, weapons, ammo, shield)
        initUI();

        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SOURCE_QUALITY);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            }
        });
        mShootSound = mSoundPool.load(this, R.raw.m4a1single, 1);
        Player.getInstance().registerForUpdates(this);
        Team.getInstance().registerForUpdates(this);
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mScoreboard.cleanup();
        mapAss.cleanup();
    }

    //Clears back stack and finishes activity. Returns to new MenuActivity
    @Override
    public void onBackPressed(){
        Player.getInstance().unregisterForUpdates(this);
        Team.getInstance().unregisterForUpdates(this);
        Game.getInstance().unregisterForUpdates(this);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void initUI(){
        //if you left FPS and someone killed you while out of FPS
        if (Player.getInstance().getRealHealth()<=0){
            if (Game.getInstance().getGameType() == GameType.VIP && !Team.getInstance().isCaptainDead()) {
                showVIPRespawnDialog();
            } else {
                startRespawnDialog("You Died!");
            }
        }
        Player.getInstance().getShield().setShieldState(new ReadyShieldState(), mShieldText, mShieldImage);
        updateHealthText();
        updateAmmoText();
        updateWeaponText();
    }

    private void respawn(){
        //reset health, weapons and shield
        Player.respawn();
        Player.getInstance().getShield().setShieldState(new ReadyShieldState(), mShieldText, mShieldImage);
        updateHealthText();
        updateAmmoText();
        updateWeaponText();
    }

    @Override
    public void notifyPlayerUpdated() {
        //no op
    }

    // health has decreased
    @Override
    public void notifyPlayerHealthDecremented(){
        Player.getInstance().getShield().updateUI(mShieldText, mShieldImage);
        updateHealthText();

        //flash the screen red
        mScreenFlash.setVisibility(View.VISIBLE);
        Handler flashHandler = new Handler();
        flashHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScreenFlash.setVisibility(View.INVISIBLE);
            }
        }, 500);

        if (Player.getInstance().getRealHealth()<=0){
            if (Game.getInstance().getGameType() != GameType.VIP) {
                //regular death
                startRespawnDialog("You Died!");
            } else if (!Player.getInstance().isCaptain()){
                //vip, not captain died
                showVIPRespawnDialog();
            } else {
                //captain died - this gets reset by new captain on respawn
                Team.getInstance().saveCaptainDead(true);
                //I will no longer be the captain (maybe)
                Team.getInstance().resetTeamCaptain();
                startRespawnDialog("You Died!");
            }
        }
    }

    @Override
    public void notifyGameUpdated() {
        // no op for now
    }

    @Override
    public void notifyGameLoaded(){
        final CountDownTimer timer = new CountDownTimer(TIMER_LENGTH_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mGameLoadingAlertDialog.setMessage((millisUntilFinished/1000)+" seconds");
            }

            @Override
            public void onFinish() {
                mGameLoadingAlertDialog.dismiss();

                //save start time locally
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("gameStartTime"+Game.getInstance().getKey(), System.currentTimeMillis());
                editor.apply();

                returnToGame();
            }
        };
        timer.start();
    }

    @Override
    public void notifyGameReady(){
        //no op
    }

    @Override
    public void notifyGameOver(){
        mapAss.cleanup();
        //save a reference to this game for viewing stats
        Player.getInstance().archiveGame(Game.getInstance().getKey());

        Player.getInstance().unregisterForUpdates(FPSActivity.this);
        Team.getInstance().unregisterForUpdates(this);
        Game.getInstance().unregisterForUpdates(this);
        mScoreboard.endGame();
        mGameOver.endGame();

        //if I'm the host and the game is over, move the game to finished games
        // table after other devices have had a chance to refresh their Games
        if (Game.getInstance() != null && Player.getInstance().getName().equals(Game.getInstance().getHost())) {
            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                public void run() {
                    Game.getInstance().endGame();
                }
            }, 2000);
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().remove("gameStartTime"+Game.getInstance().getKey()).apply();
        settings.edit().remove("totalHits"+Game.getInstance().getKey()).apply();
        settings.edit().remove("totalShots"+Game.getInstance().getKey()).apply();
    }

    @Override
    public void notifyGameDeleted(){
        //better not happen
    }

    @Override
    public void notifyGameScoreUpdated() {
        mScoreboard.notifyScoreUpdated();
    }

    @Override
    public void notifyTeamUpdated(){
        //update UI accordingly
    }

    @Override
    public void notifyTeamCaptainDied() {
        startVIPRespawnTimer();
    }

    private void showVIPRespawnDialog(){
        mVIPRespawnDialog = new AlertDialog.Builder(FPSActivity.this).create();
        mVIPRespawnDialog.setTitle("You Died!");
        mVIPRespawnDialog.setMessage("Cannot respawn until team captain is eliminated.");
        mVIPRespawnDialog.setCancelable(false);
        mVIPRespawnDialog.show();
        hideSystemUI();
    }

    private void startVIPRespawnTimer(){
        if (mVIPRespawnDialog!=null && mVIPRespawnDialog.isShowing()) {
            mVIPRespawnDialog.dismiss();
        }
        startRespawnDialog("Your team captain died!");
    }

    private void startRespawnDialog(String deathMessage){
        final AlertDialog alertDialog = new AlertDialog.Builder(FPSActivity.this).create();
        alertDialog.setTitle(deathMessage);
        alertDialog.setMessage("Respawning in " + (RESPAWN_TIMER_LENGTH_MS / 1000) + " seconds");
        alertDialog.setCancelable(false);
        alertDialog.show();
        hideSystemUI();
        new CountDownTimer(RESPAWN_TIMER_LENGTH_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (Game.getInstance().isGameOver()) {
                    alertDialog.dismiss();
                }
                alertDialog.setMessage("Respawning in " + (millisUntilFinished/1000)+" seconds");
            }

            @Override
            public void onFinish() {
                if (!Game.getInstance().isGameOver()) {
                    alertDialog.dismiss();

                    //reset team's captainDead if I'm the new captain
                    if (Game.getInstance().getGameType() == GameType.VIP && Player.getInstance().isCaptain()){
                        Team.getInstance().saveCaptainDead(false);
                    }

                    //reset health, weapons and shields
                    respawn();
                    hideSystemUI();
                }
            }
        }.start();
    }

    private void startLoading(){
        mGameLoadingAlertDialog = new AlertDialog.Builder(this).create();
        mGameLoadingAlertDialog.setTitle("Game Starting in:");
        mGameLoadingAlertDialog.setMessage("Waiting on players...");
        mGameLoadingAlertDialog.setCancelable(false);
        mGameLoadingAlertDialog.show();

        if (Game.getInstance().getGameType() == GameType.VIP && Game.getInstance().getHost().equals(Player.getInstance().getName())){
            Game.getInstance().initGameCaptains();
        }

        Player.getInstance().loadUp();
        hideSystemUI();
    }

    private void returnToGame(){
        //load local start time, shots, and hits
        ColorShooterTask.resetColorMap();
        mapAss.clearGoogleMap();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Player.getInstance().setTotalHits(settings.getInt("totalHits"+Game.getInstance().getKey(),0));
        Player.getInstance().setTotalShots(settings.getInt("totalShots" + Game.getInstance().getKey(), 0));

        startGameTime(settings.getLong("gameStartTime"+Game.getInstance().getKey(), 0L));
    }

    private void startGameTime(long time){
        if (Game.getInstance().getTimeEnabled()){
            long timeLeft = Game.getInstance().getEndMinutes()*60*1000 - (System.currentTimeMillis() - time);
            new CountDownTimer(timeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTimeText.setText(String.format("%02d:%02d",(millisUntilFinished/(1000*60))%60,(millisUntilFinished/1000)%60));
                }

                @Override
                public void onFinish() {
                   Game.getInstance().saveGameOver();
                }
            }.start();
        } else {
            Chronometer stopwatch = (Chronometer) findViewById(R.id.chronometer_fps);
            stopwatch.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - time));
            stopwatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    mTimeText.setText(chronometer.getText());
                }
            });
            stopwatch.start();
        }
    }

    // health values read from singleton Player
    private static void updateHealthText(){
        mHealthText.setText(String.valueOf(Player.getInstance().getRealHealth()));
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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_UP && !Game.getInstance().isGameOver()) {
            if (event.getY() > touchDownY + 200){
                if (hudVisible) {
                    //swipe down - hide HUD
                    Animation slideOff = AnimationUtils.loadAnimation(this, R.anim.slide_off);
                    mHUD.startAnimation(slideOff);
                    //would use setVisibility(invisible) but SurfaceView bugs out
                    mHUD.setVisibility(View.GONE);
                    hudVisible = false;
                }
            }else if (event.getY() < touchDownY - 200) {
                if (!hudVisible) {
                    //swipe up - show HUD
                    Animation slideOn = AnimationUtils.loadAnimation(this, R.anim.slide_on);
                    mHUD.startAnimation(slideOn);
                    mHUD.setVisibility(View.VISIBLE);
                    hudVisible = true;
                }
            }
        }

        ColorShooterTask asyncTask = new ColorShooterTask(new ShooterCallback() {

            @Override
            public void onFinishShoot(String teamPlayerHit) {
                Player.getInstance().incTotalShots();
                getSharedPreferences(PREFS_NAME, 0).edit().putInt("totalShots" + Game.getInstance().getKey(), Player.getInstance().getTotalShots()).apply();
                if (!teamPlayerHit.equals("")) {
                    String teamName = teamPlayerHit.split(":~")[0];
                    final String playerName = teamPlayerHit.split(":~")[1];
                    if (Player.getInstance().decrementHealthAndIncMyScore(Player.getInstance().retrieveActiveWeapon().getStrength(), Game.getInstance().getReference().child("teams").child(teamName).child("players").child(playerName))) {
                        Player.getInstance().incTotalHits();
                        getSharedPreferences(PREFS_NAME, 0).edit().putInt("totalHits" + Game.getInstance().getKey(), Player.getInstance().getTotalHits()).apply();

                        if (Game.getInstance().getTeams().get(teamName).getPlayers().get(playerName).getHealth() != 0) {
                            mReticle.showHitAnimation(playerName);
                        } else {
                            mReticle.showDeadAnimation(playerName);
                        }
                    }
                }
            }

            @Override
            public void updateGUI() {
                updateGunUI();
            }
        });
        asyncTask.execute(mPreview.getCameraData());

        return true;
    }

    //onClick listener
    public void deployShield(View view){
        Player.getInstance().deployShield(mShieldText, mShieldImage);
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
        Player.getInstance().saveLocation(location);
        mapAss.updateMyMarker(location.getLatitude(),location.getLongitude());
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