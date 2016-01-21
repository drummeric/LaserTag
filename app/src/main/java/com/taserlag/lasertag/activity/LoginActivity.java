package com.taserlag.lasertag.activity;

import com.parse.ui.ParseLoginDispatchActivity;

public class LoginActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return MainMenuActivity.class;
    }

}