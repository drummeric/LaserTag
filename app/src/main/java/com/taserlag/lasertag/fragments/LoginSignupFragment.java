package com.taserlag.lasertag.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.application.LaserTagApplication;
import com.taserlag.lasertag.player.DBUser;

import java.util.Map;

public class LoginSignupFragment extends Fragment {

    private final int MIN_INPUT_LENGTH = 5;
    private final int MIN_EMAIL_LENGTH = 6;

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
     */
    public void performSignup() {
        final EditText usernameText = ((EditText) getView().findViewById(R.id.edit_text_signup_username));
        final EditText passwordText = ((EditText) getView().findViewById(R.id.edit_text_signup_password));
        final EditText passwordConfirmText = ((EditText) getView().findViewById(R.id.edit_text_signup_password_confirm));
        final EditText emailText = ((EditText) getView().findViewById(R.id.edit_text_signup_email));
        boolean validSignupCredentials = true;

        usernameText.setError(null);
        passwordText.setError(null);
        passwordConfirmText.setError(null);
        emailText.setError(null);

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirm = passwordConfirmText.getText().toString();
        String email = emailText.getText().toString();

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

        if (validSignupCredentials){
            doSignup(username,password,email);
        }
    }

    private void doSignup(final String username, final String password, final String email){
        final ProgressDialog PD = new ProgressDialog(getActivity());
        PD.setTitle("Please Wait..");
        PD.setMessage("Loading...");
        PD.setCancelable(false);
        PD.show();

        LaserTagApplication.firebaseReference.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {
                Log.i(TAG, "Signed up a user with id: " + stringObjectMap.get("uid"));

                DBUser newUser = new DBUser(username);
                newUser.setColor(new float[]{0, 0, 0});//new users get black set as initial color
                LaserTagApplication.firebaseReference.child("users").child((String) stringObjectMap.get("uid")).setValue(newUser);

                CharSequence text = username + ", your account has been created.";
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                // Creating account does not log the user in
                LoginFragment lf = (LoginFragment) getActivity().getSupportFragmentManager().findFragmentByTag("login_fragment");
                lf.doLogin(PD,email,password);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                PD.dismiss();
                CharSequence text;
                switch (firebaseError.getCode()) {
                    case FirebaseError.EMAIL_TAKEN:
                        Log.e(TAG, "Signup Failure: email taken", firebaseError.toException());
                        text = getString(R.string.signup_error_email_taken);
                        break;
                    case FirebaseError.INVALID_EMAIL:
                        Log.e(TAG, "Signup Failure: invalid email", firebaseError.toException());
                        text = getString(R.string.signup_error_email_invalid);
                        break;
                    case FirebaseError.NETWORK_ERROR:
                        Log.e(TAG, "Signup Failure: network error", firebaseError.toException());
                        text = getString(R.string.network_error);
                        break;
                    default:
                        Log.e(TAG, "Signup Failure: Unknown error", firebaseError.toException());
                        text = getString(R.string.login_error_unknown);
                        break;
                }
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
