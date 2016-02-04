package com.taserlag.lasertag.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.Player;

public class LoginSignupFragment extends Fragment {

    private final int MIN_INPUT_LENGTH = 5;
    private final int MIN_EMAIL_LENGTH = 6;
    private final int MIN_NAME_LENGTH = 2;

    private final String TAG = "LoginSignupFragment";
    private OnFragmentInteractionListener mListener;

    public LoginSignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_signup, container, false);
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
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*
     * RULES:
     * username >= 5
     * password >= 5
     * passwordConfirm = password
     * email contains @ and >= 6
     * name >= 2
     */
    public void performSignup() {
        final EditText usernameText = ((EditText) getView().findViewById(R.id.edit_text_signup_username));
        final EditText passwordText = ((EditText) getView().findViewById(R.id.edit_text_signup_password));
        final EditText passwordConfirmText = ((EditText) getView().findViewById(R.id.edit_text_signup_password_confirm));
        final EditText emailText = ((EditText) getView().findViewById(R.id.edit_text_signup_email));
        final EditText nameText = ((EditText) getView().findViewById(R.id.edit_text_signup_name));
        boolean validSignupCredentials = true;

        usernameText.setError(null);
        passwordText.setError(null);
        passwordConfirmText.setError(null);
        emailText.setError(null);
        nameText.setError(null);

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirm = passwordConfirmText.getText().toString();
        String email = emailText.getText().toString();
        String name = nameText.getText().toString();

        if (username.length()<MIN_INPUT_LENGTH){
            usernameText.setError(getString(R.string.login_error_username_invalid_length));
            validSignupCredentials = false;
        }

        if (password.length()<MIN_INPUT_LENGTH){
            passwordText.setError(getString(R.string.login_error_password_invalid_length));
            validSignupCredentials = false;
        }

        if (!password.equals(passwordConfirm)) {
            passwordConfirmText.setError(getString(R.string.signup_error_mismatch_passwords));
            passwordText.setText("");
            passwordConfirmText.setText("");
            validSignupCredentials = false;
        }

        if (!email.contains("@") || !(email.contains(".")) || email.length() < MIN_EMAIL_LENGTH){
            emailText.setError(getString(R.string.signup_error_email_invalid));
            emailText.setText("");
            validSignupCredentials = false;
        }

        if (name.length() < MIN_NAME_LENGTH){
            nameText.setError(getString(R.string.signup_error_name_invalid));
            emailText.setText("");
            validSignupCredentials = false;
        }

        if (validSignupCredentials){
            doSignup(username,password,email,name);
        }
    }

    private void doSignup(String username, String password, final String email, final String name){

        final ProgressDialog PD = new ProgressDialog(getActivity());
        PD.setTitle("Please Wait..");
        PD.setMessage("Loading...");
        PD.setCancelable(false);
        PD.show();

        LaserTagApplication.kinveyClient.user().create(username, password, new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable t) {
                PD.dismiss();
                Log.e(TAG, "Signup Failure", t);
                Log.e(TAG, t.getMessage());

                if (t.getMessage().startsWith("UserAlreadyExists")){
                    CharSequence text = getString(R.string.signup_error_username_conflict);
                    Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } else {
                    CharSequence text = getString(R.string.signup_failure);
                    Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onSuccess(User u) {
                Log.i(TAG, "Signed up a user with id: " + u.getId());
                CharSequence text = u.getUsername() + ", your account has been created.";
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                final Player player = new Player(u.getUsername());
                AsyncAppData<Player> myplayer = LaserTagApplication.kinveyClient.appData("players", Player.class);
                myplayer.save(player, new KinveyClientCallback<Player>() {
                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "failed to save game data", e);
                    }

                    @Override
                    public void onSuccess(Player p) {
                        Log.d(TAG, "saved data for game " + p.getId());
                        LaserTagApplication.kinveyClient.user().put("playerReference", p.getId());
                        LaserTagApplication.kinveyClient.user().put("email", email);
                        LaserTagApplication.kinveyClient.user().put("first_name", name);
                        LaserTagApplication.kinveyClient.user().update(new KinveyUserCallback() {
                            @Override
                            public void onFailure(Throwable e) {
                                Log.e(TAG, "Failed to set up user fields", e);
                            }

                            @Override
                            public void onSuccess(User u) {
                                Log.i(TAG, "Set up user fields for user with id: " + u.getId());
                                LaserTagApplication.setGlobalPlayer();
                                PD.dismiss();

                                Intent i = new Intent(getActivity(), MenuActivity.class);
                                getActivity().finish();
                                startActivity(i);
                            }
                        });// saving player, email, and name to user
                    }
                });//saving player
            }
        });//creating user
    }
}
