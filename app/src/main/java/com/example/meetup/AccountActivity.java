package com.example.meetup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import com.example.meetup.events.EventsFragment;
import com.example.meetup.events.EventsFragmentViewModel;
import com.example.meetup.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends FragmentActivity {
    private BottomNavigationView menu;
    private View headerView;
    private static final String EVENTS_TITLE = "Мои мероприятия";
    private static final String PROFILE_TITLE = "Мой профиль";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        WindowCompat.enableEdgeToEdge(getWindow());

        headerView = findViewById(R.id.header_title);
        menu = findViewById(R.id.menu);
        menu.setOnApplyWindowInsetsListener(null);


        ViewCompat.setOnApplyWindowInsetsListener(headerView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            v.getLayoutParams().height += systemBars.top;
            headerView.setOnApplyWindowInsetsListener(null);
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(menu, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        initNavMenu(new EventsFragment(), EVENTS_TITLE);
        menu.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.profile) {
                initNavMenu(new ProfileFragment(), PROFILE_TITLE);
                return true;
            }
            if (menuItem.getItemId() == R.id.events) {
                initNavMenu(new EventsFragment(), EVENTS_TITLE);
                return true;
            }

            return false;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG", "AccountActivity onResume - обновляем данные");
        EventsFragmentViewModel viewModel = new ViewModelProvider(this).get(EventsFragmentViewModel.class);
    }

    private void initNavMenu(Fragment fragment, String header_title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TextView header = headerView.findViewById(R.id.header_title_text);
        header.setText(header_title);
        fragmentTransaction.replace(R.id.frame_layout_account, fragment);
        fragmentTransaction.commit();
    }
}