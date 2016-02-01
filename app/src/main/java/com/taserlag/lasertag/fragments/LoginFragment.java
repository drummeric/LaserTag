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

public class LoginFragment extends Fragment {

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

    public void performLogin() {
        final EditText username = ((EditText) getView().findViewById(R.id.edit_text_login_username));
        final EditText password = ((EditText) getView().findViewById(R.id.edit_text_login_password));
        LaserTagApplication.kinveyClient.user().login(username.getText().toString(), password.getText().toString(), new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Login Failure", t);
                CharSequence text = "Wrong username or password.";
                Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                username.setText("");
                password.setText("");
            }

            @Override
            public void onSuccess(User u) {
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
