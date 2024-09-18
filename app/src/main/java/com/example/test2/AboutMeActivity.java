package com.example.test2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutMeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_analytics);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent homeIntent = new Intent(AboutMeActivity.this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.Info) {
                Intent infoIntent = new Intent(AboutMeActivity.this, InfoActivity.class);
                startActivity(infoIntent);
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                return true;
            } else if (itemId == R.id.navigation_settings) {
                finishAffinity(); // Exit the application
                return true;
            }
            return false;
        });
    }
}
