package com.android.aku.fcmchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.android.aku.fcmchat.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Shows up initially to determine the flow of the app
 */
public class MainActivity extends AppCompatActivity {
    private static final int WAIT_TIME_MS = 1000;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        mRunnable = new Runnable() {
            @Override
            public void run() {
                // check if user is already logged in or not
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // if logged in redirect the user to user list activity
                    startActivity(new Intent(MainActivity.this, UserListActivity.class));
                } else {
                    // otherwise redirect the user to login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                finish();
            }
        };

        mHandler.postDelayed(mRunnable, WAIT_TIME_MS);
    }
}
