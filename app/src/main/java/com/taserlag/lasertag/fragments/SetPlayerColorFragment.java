package com.taserlag.lasertag.fragments;

import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.camera.CameraHelper;
import com.taserlag.lasertag.camera.CameraPreview;
import com.taserlag.lasertag.game.Game;
import com.taserlag.lasertag.player.DBPlayerStats;

public class SetPlayerColorFragment extends Fragment {
    private static final String PLAYER_NAME_PARAM = "playerName";
    private static final String TEAM_NAME_PARAM = "teamName";

    private final String TAG = "SetPlayerColorFragment";

    private Camera mCamera;
    private CameraPreview mPreview;
    private int mShots = 0;
    private int[] mARGB = new int[4];

    private String mPlayerName = "";
    private String mTeamName = "";

    private OnFragmentInteractionListener mListener;

    public SetPlayerColorFragment() {
        // Required empty public constructor
    }

    public static SetPlayerColorFragment newInstance(String playerName, String teamName) {
        SetPlayerColorFragment fragment = new SetPlayerColorFragment();
        Bundle args = new Bundle();
        args.putString(PLAYER_NAME_PARAM, playerName);
        args.putString(TEAM_NAME_PARAM, teamName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlayerName = getArguments().getString(PLAYER_NAME_PARAM);
            mTeamName = getArguments().getString(TEAM_NAME_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_player_color, container, false);

        final TextView playerColorPrompt = (TextView) view.findViewById(R.id.text_view_set_player_color_prompt);

        playerColorPrompt.setText("Shoot " + mPlayerName + " " + (5 - mShots) + " times.");

        mCamera = getCameraInstance();

        // Create preview of camera
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.set_player_color_camera_preview);
        preview.addView(mPreview);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if ((mPreview.getCameraData()!=null) && (++mShots <= 5)) {
                        if (!mPlayerName.equals("")) {
                            playerColorPrompt.setText("Shoot " + mPlayerName + " " + (5 - mShots) + " times.");
                        }

                        int[] argb = CameraHelper.getInstance().getTargetColor(mPreview.getCameraData());

                        //running average of colors detected
                        mARGB[0] = (mARGB[0]*(mShots-1) + argb[0])/(mShots);
                        mARGB[1] = (mARGB[1]*(mShots-1) + argb[1])/(mShots);
                        mARGB[2] = (mARGB[2]*(mShots-1) + argb[2])/(mShots);
                        mARGB[3] = (mARGB[3]*(mShots-1) + argb[3])/(mShots);

                        if (mShots == 5) {
                            Log.i(TAG, "Average recorded argb: "+ mARGB[0] + " " + mARGB[1] + " " + mARGB[2] + " " + mARGB[3] + ".");

                            //save player color to database and pop back to GameLobby
                            DBPlayerStats.saveColor(mARGB, Game.getInstance().getReference().child("teams").child(mTeamName).child("players").child(mPlayerName).child("playerStats"));
                            Toast.makeText(getContext(), mPlayerName + "'s color updated!", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        releaseCamera();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // Attempt to get a Camera instance
            c.setDisplayOrientation(90);
        } catch (Exception e) {
            Log.e(TAG, "Camera is not available", e); // Camera is not available (in use or does not exist)
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
}
