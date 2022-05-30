package com.example.connectme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.connectme.R;



public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
       // getSupportActionBar().setDisplayHomeAsUpEnabled(false);
     int   secondsDelayed =2;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(Splash.this, PhoneNumberActivity.class));
                finish();
            }
        }, secondsDelayed * 1000);

    }
}