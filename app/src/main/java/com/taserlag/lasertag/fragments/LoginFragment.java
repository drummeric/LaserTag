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

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;

public class LoginFragment extends Fragment {

    private final int MIN_PASSWORD_LENGTH = 5;
    private final int MIN_EMAIL_LENGTH = 6;

    private final String TAG = "LoginFragment";
    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
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
     * Email is valid format ( 6 characters and contains @ and .
     * Password >= 5 characters long
     */
    public void performLogin() {
        final EditText emailText = ((EditText) getView().findViewById(R.id.edit_text_login_email));
        final EditText passwordText = ((EditText) getView().findViewById(R.id.edit_text_login_password));
        boolean validLoginCredentials = true;
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        emailText.setError(null);
        passwordText.setError(null);

        if (!email.contains("@") || !(email.contains(".")) || email.length() < MIN_EMAIL_LENGTH){
            emailText.setError(getString(R.string.signup_error_email_invalid));
            emailText.setText("");
            validLoginCredentials = false;
        }

        if (password.length()<MIN_PASSWORD_LENGTH){
            passwordText.setError(getString(R.string.login_error_password_invalid_length));
            validLoginCredentials = false;
        }

        if (validLoginCredentials){
            final ProgressDialog PD = new ProgressDialog(getActivity());
            PD.setTitle("Please Wait..");
            PD.setMessage("Loading...");
            PD.setCancelable(false);
            PD.show();
            doLogin(PD,email,password);
        } else {
            clearEntries();
        }

    }

    private void clearEntries(){
        ((EditText) getView().findViewById(R.id.edit_text_login_email)).setText("");
        ((EditText) getView().findViewById(R.id.edit_text_login_password)).setText("");
    }

    public void doLogin(final ProgressDialog PD, String email,String password){
        LaserTagApplication.firebaseReference.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                PD.dismiss();
                Log.i(TAG, "Logged in a user with id: " + authData.getUid());
                Intent i = new Intent(getActivity(), MenuActivity.class);
                getActivity().finish();
                startActivity(i);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                PD.dismiss();
                CharSequence text;
                switch (firebaseError.getCode()) {
                    case FirebaseError.USER_DOES_NOT_EXIST:
                        Log.e(TAG, "Login Failure: User does not exist", firebaseError.toException());
                        text = getString(R.string.login_error_user_dne);
                        break;
                    case FirebaseError.INVALID_PASSWORD:
                        Log.e(TAG, "Login Failure: Invalid password", firebaseError.toException());
                        text = getString(R.string.login_error_wrong_password);
                        break;
                    case FirebaseError.NETWORK_ERROR:
                        Log.e(TAG, "Login Failure: network error", firebaseError.toException());
                        text = getString(R.string.network_error);
                        break;
                    default:
                        Log.e(TAG, "Login Failure: Unknown error", firebaseError.toException());
                        text = getString(R.string.login_error_unknown);
                        break;
                }
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                clearEntries();
            }
        });
    }
}
