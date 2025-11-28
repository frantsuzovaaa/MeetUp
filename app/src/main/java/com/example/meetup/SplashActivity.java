package com.example.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends ComponentActivity {
    private static final int SPLASH_DELAY = 2000;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_splash);
        firebaseAuth = FirebaseAuth.getInstance();


        new Handler().postDelayed(() -> {
            Intent intent;
            if (firebaseAuth.getCurrentUser()!=null){
                intent = new Intent(SplashActivity.this, AccountActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish();
            }

        }, SPLASH_DELAY);
    }
}
