package com.taserlag.lasertag.fragments;

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

public class LoginSignupFragment extends Fragment {

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

    public void performSignup() {
        final EditText username = ((EditText) getView().findViewById(R.id.edit_text_signup_username));
        final EditText password = ((EditText) getView().findViewById(R.id.edit_text_signup_password));
        final EditText passwordConfirm = ((EditText) getView().findViewById(R.id.edit_text_signup_password_confirm));
        final EditText email = ((EditText) getView().findViewById(R.id.edit_text_signup_email));
        final EditText name = ((EditText) getView().findViewById(R.id.edit_text_signup_name));

        if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
            passwordConfirm.setError("Your passwords must match");
            return;
        }

        LaserTagApplication.kinveyClient.user().create(username.getText().toString(), password.getText().toString(), new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Signup Failure", t);
                CharSequence text = "Could not sign up.";
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(User u) {
                Log.i(TAG, "Signed up a user with id: " + u.getId());
                CharSequence text = u.getUsername() + ", your account has been created.";
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                LaserTagApplication.kinveyClient.user().put("email", email.getText().toString());
                LaserTagApplication.kinveyClient.user().put("first_name", name.getText().toString());
                LaserTagApplication.kinveyClient.user().update(new KinveyUserCallback() {
                    @Override
                    public void onFailure(Throwable e) {Log.e(TAG, "Failed to set up user fields", e);}

                    @Override
                    public void onSuccess(User u) {Log.i(TAG, "Set up user fields for user with id: " + u.getId());}
                });

                Intent i = new Intent(getActivity(), MenuActivity.class);
                getActivity().finish();
                startActivity(i);
            }
        });
    }
}
