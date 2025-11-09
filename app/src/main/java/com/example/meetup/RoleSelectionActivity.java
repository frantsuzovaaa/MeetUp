package com.example.meetup;


import android.os.Bundle;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.meetup.login.LogInFragment;

public class RoleSelectionActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);
        WindowCompat.enableEdgeToEdge(getWindow());
        View view = findViewById(R.id.role_selection_container);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LogInFragment())
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insetsCompat) -> {
                    Insets systemBars = insetsCompat.getInsets(WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return WindowInsetsCompat.CONSUMED;
                }
        );


    }
}