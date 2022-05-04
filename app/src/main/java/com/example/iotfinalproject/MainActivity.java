package com.example.iotfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onResume() {
        super.onResume();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                startActivity(intent);
                finish();
            }

        }, 4000);

    }
    @Override
    protected void onPause() {
        super.onPause();

        if (timer != null) {
            timer.cancel();
        }

    }
}