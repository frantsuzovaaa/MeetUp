package com.example.meetup;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meetup.databinding.ActivityInfoEventBinding;
import com.example.meetup.events.Events;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventInfoActivity extends FragmentActivity {

    private View headerView;
    private String currentId;

    private EventsInfoShareViewModel shareViewModel;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagesFragmentAdapter adapter;

    private ActivityInfoEventBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInfoEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tabLayout = binding.headerTabs;
        viewPager = binding.viewPager;

        currentId = getIntent().getStringExtra("event_id");
        if (currentId == null || currentId.isEmpty()) {
            Toast.makeText(this, "Произошла ошибка", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

       
        shareViewModel = new ViewModelProvider(this).get(EventsInfoShareViewModel.class);
        loadEventDataFromFirebase(currentId);


        adapter = new ViewPagesFragmentAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Информация");
                tab.setIcon(R.drawable.icon_info_event);
            } else if (position == 1) {
                tab.setText("Гости");
                tab.setIcon(R.drawable.members);
            } else if (position == 2) {
                tab.setText("Статистика");
                tab.setIcon(R.drawable.statistic_icon);
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    Fragment f = adapter.getFragment(2);
                    if (f instanceof StatisticFragment) {
                        ((StatisticFragment) f).refreshData();
                    }
                }
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    Fragment f = adapter.getFragment(2);
                    if (f instanceof StatisticFragment) {
                        ((StatisticFragment) f).refreshData();
                    }
                }
            }
        });


        headerView = binding.headerTabs;

        EdgeToEdge.enable(this);
        WindowCompat.enableEdgeToEdge(getWindow());

        ViewCompat.setOnApplyWindowInsetsListener(headerView, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.systemBars()
            );
            v.setPadding(0, systemBars.top, 0, 0);
            v.getLayoutParams().height += systemBars.top;
            headerView.setOnApplyWindowInsetsListener(null);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void loadEventDataFromFirebase(String currentId) {
        FirebaseDatabase firebaseDatabase =
                FirebaseDatabase.getInstance("https://meetup2-a8e75-default-rtdb.europe-west1.firebasedatabase.app");

        firebaseDatabase.getReference()
                .child("Events")
                .child(currentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Events event = dataSnapshot.getValue(Events.class);
                            if (event != null) {
                                // Шарим объект ивента и его id во ViewModel
                                shareViewModel.setEvent(event, currentId);
                            }
                        } else {
                            Toast.makeText(EventInfoActivity.this,
                                    "Произошла ошибка с чтением данных",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EventInfoActivity.this,
                                "Произошла ошибка с чтением данных",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    public static class ViewPagesFragmentAdapter extends FragmentStateAdapter {

        private final Fragment[] fragments;

        public ViewPagesFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);

            fragments = new Fragment[]{
                    new EventInfoFragment(),
                    new MembersFragment(),
                    new StatisticFragment()
            };
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments[position];
        }

        @Override
        public int getItemCount() {
            return fragments.length;
        }

        public Fragment getFragment(int position) {
            return fragments[position];
        }
    }
}
