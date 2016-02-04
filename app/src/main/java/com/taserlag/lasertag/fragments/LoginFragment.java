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

import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.taserlag.lasertag.R;
import com.taserlag.lasertag.activity.MenuActivity;
import com.taserlag.lasertag.application.LaserTagApplication;

public class LoginFragment extends Fragment {

    private final int MIN_INPUT_LENGTH = 5;

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
     * Username >= 5 characters long
     * Password >= 5 characters long
     */
    public void performLogin() {
        final EditText usernameText = ((EditText) getView().findViewById(R.id.edit_text_login_username));
        final EditText passwordText = ((EditText) getView().findViewById(R.id.edit_text_login_password));
        boolean validLoginCredentials = true;
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        usernameText.setError(null);
        passwordText.setError(null);

        if (username.length()<MIN_INPUT_LENGTH){
            usernameText.setError(getString(R.string.login_error_username_invalid_length));
            validLoginCredentials = false;
        }

        if (password.length()<MIN_INPUT_LENGTH){
            passwordText.setError(getString(R.string.login_error_password_invalid_length));
            validLoginCredentials = false;
        }

        if (validLoginCredentials){
            doLogin(username,password);
        } else {
            clearEntries();
        }

    }

    private void clearEntries(){
        ((EditText) getView().findViewById(R.id.edit_text_login_username)).setText("");
        ((EditText) getView().findViewById(R.id.edit_text_login_password)).setText("");
    }

    private void doLogin(String username,String password){
        final ProgressDialog PD = new ProgressDialog(getActivity());
        PD.setTitle("Please Wait..");
        PD.setMessage("Loading...");
        PD.setCancelable(false);
        PD.show();

        LaserTagApplication.kinveyClient.user().login(username, password, new KinveyUserCallback() {

            @Override
            public void onFailure(Throwable t) {
                PD.dismiss();
                Log.e(TAG, "Login Failure", t);
                CharSequence text = getString(R.string.login_error_wrong_credentials);
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                clearEntries();
            }

            @Override
            public void onSuccess(User u) {
                LaserTagApplication.setGlobalPlayer();
                PD.dismiss();
                Log.i(TAG,"Logged in a user with id: " + u.getId());
                CharSequence text = "Welcome back, " + u.getUsername() + ".";
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getActivity(), MenuActivity.class);
                getActivity().finish();
                startActivity(i);
            }
        });
    }
}
