package com.taserlag.lasertag.activity;


import android.content.Intent;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.taserlag.lasertag.R;


public class LoginActivity extends AppCompatActivity {

    // Valid credentials are: "fooname:hello", "barname:world"

    // UI references.
    private EditText usernameView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameView = (EditText) findViewById(R.id.textUsername);
        passwordView = (EditText) findViewById(R.id.textPassword);
    }

    public void attemptLogin(View view) {

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(username)){
            usernameView.setError(getString(R.string.error_empty_field));
            cancel = true;
        }

        if (TextUtils.isEmpty(password)){
            passwordView.setError(getString(R.string.error_empty_field));
            cancel = true;
        }

        if (cancel) {
            reset();
        } else {
            validate(username, password);
        }
    }

    private void validate(String username, String password){
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    login();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    reset();
                }
            }
        });
    }

    private void login() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    private void reset() {
        usernameView.setText("");
        passwordView.setText("");
        Toast t = Toast.makeText(getApplicationContext(), "Invalid credentials",Toast.LENGTH_SHORT );
        t.setGravity(Gravity.TOP,0,0);
        t.show();
    }

}
