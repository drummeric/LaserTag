package com.taserlag.lasertag.fragments;

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

public class LoginPasswordRecoverFragment extends Fragment {

    private final int MIN_EMAIL_LENGTH = 6;


    private final String TAG = "LoginPasswordRecoverFragment";
    private OnFragmentInteractionListener mListener;

    public LoginPasswordRecoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_password_recover, container, false);
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

    public void performPasswordRecover() {
        final EditText emailText = ((EditText) getView().findViewById(R.id.edit_text_recover_password_email));

        final String email = emailText.getText().toString();

        emailText.setError(null);

        if (!email.contains("@") || !(email.contains(".")) || email.length() < MIN_EMAIL_LENGTH){
            emailText.setError(getString(R.string.signup_error_email_invalid));
            emailText.setText("");
        } else {
            LaserTagApplication.firebaseReference.resetPassword(email, new Firebase.ResultHandler() {
                @Override
                public void onSuccess() {
                    // password reset email sent
                    Log.i(TAG, "Password reset email sent to: " + email);
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.password_recover_email_sent), Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                @Override
                public void onError(FirebaseError firebaseError) {
                    // error encountered
                    CharSequence text;
                    switch (firebaseError.getCode()) {
                        case FirebaseError.USER_DOES_NOT_EXIST:
                            Log.e(TAG, "Login Failure: User does not exist", firebaseError.toException());
                            text = getString(R.string.password_recover_error_user_dne);
                            break;
                        case FirebaseError.NETWORK_ERROR:
                            Log.e(TAG, "Login Failure: network error", firebaseError.toException());
                            text = getString(R.string.network_error);
                            break;
                        default:
                            Log.e(TAG, "Password recover error: Unknown error", firebaseError.toException());
                            text = getString(R.string.password_recover_failure);
                            break;
                    }
                    Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    emailText.setText("");
                }
            });
        }
    }
}
