package com.taserlag.lasertag.activity;


import android.content.Intent;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.taserlag.lasertag.R;


public class LoginActivity extends AppCompatActivity {

    private String[] testCredentials = new String[]{
            "fooname:hello", "barname:world"
    };

    // UI references.
    private EditText usernameView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameView = (EditText) findViewById(R.id.textUsername);
        passwordView = (EditText) findViewById(R.id.textPassword);

        Button signInButton = (Button) findViewById(R.id.buttonSignin);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void attemptLogin() {

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


        if (cancel || !validate(username, password)) {
            usernameView.setText("");
            passwordView.setText("");
            Toast t = Toast.makeText(getApplicationContext(), "Invalid credentials",Toast.LENGTH_SHORT );
            t.setGravity(Gravity.TOP,0,0);
            t.show();
        }else {
            Intent intent = new Intent(this, FPSActivity.class);
            startActivity(intent);
        }

    }

    private boolean validate(String username, String password){
        String credentials = username + ":" + password;
        for (String s : testCredentials){
            if(credentials.equals(s)){
                return true;
            }
        }
        return false;
    }


}
