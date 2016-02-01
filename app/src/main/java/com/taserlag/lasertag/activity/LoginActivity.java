package com.taserlag.lasertag.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.taserlag.lasertag.R;
import com.taserlag.lasertag.fragments.LoginFragment;
import com.taserlag.lasertag.fragments.LoginPasswordRecoverFragment;
import com.taserlag.lasertag.fragments.LoginSignupFragment;

public class LoginActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener,
        LoginSignupFragment.OnFragmentInteractionListener,
        LoginPasswordRecoverFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            LoginFragment lf = new LoginFragment();
            addFragment(R.id.login_frame, lf, "login_fragment");
        }
    }

    protected void addFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewID, fragment, tag)
                .commit();
    }

    protected void replaceFragment(int containerViewID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewID, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    private Fragment findFragmentByTag(String tag){
        return getSupportFragmentManager()
                .findFragmentByTag(tag);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void showLoginSignup(View view) {
        LoginSignupFragment fragment = new LoginSignupFragment();
        replaceFragment(R.id.login_frame, fragment, "login_signup_fragment");
    }

    public void showLoginPasswordRecover(View view) {
        LoginPasswordRecoverFragment fragment = new LoginPasswordRecoverFragment();
        replaceFragment(R.id.login_frame, fragment, "login_password_recover_fragment");
    }

    public void login(View view) {
        ((LoginFragment) findFragmentByTag("login_fragment")).performLogin();
    }

    public void signup(View view) {
        ((LoginSignupFragment) findFragmentByTag("login_signup_fragment")).performSignup();
    }
}