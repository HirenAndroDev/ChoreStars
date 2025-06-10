package com.chores.app.kids.chores_app_for_kids.services;


import android.app.Application;

import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize sound helper for kid features
        SoundHelper.initialize(this);
    }
}
