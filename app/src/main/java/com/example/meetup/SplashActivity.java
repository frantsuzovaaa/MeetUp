package com.example.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class SplashActivity extends ComponentActivity {
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
