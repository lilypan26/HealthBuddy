package com.example.iotfinalproject;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeNavigation {

    private HomeNavigation(){

    }

    public static void createHomeNavigation(AppCompatActivity activity) {
        Toolbar myToolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(myToolbar);
        ImageView home = (ImageView) activity.findViewById(R.id.home_icon);
        Intent intent = new Intent(activity, UserHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(intent);
            }
        });
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = activity.getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
