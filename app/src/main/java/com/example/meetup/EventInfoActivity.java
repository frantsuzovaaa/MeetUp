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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meetup.databinding.ActivityInfoEventBinding;
import com.example.meetup.events.Events;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventInfoActivity extends FragmentActivity {
    private View headerView;
    private View buttonView;
    private String currentId;

    private EventsInfoShareViewModel shareViewModel;

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagesFragmentAdapter adapter;
    private ActivityInfoEventBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInfoEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tabLayout = binding.headerTabs;
        viewPager = binding.viewPager;
        currentId = getIntent().getStringExtra("event_id");
        adapter = new ViewPagesFragmentAdapter(this,tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        if (currentId == null || currentId.isEmpty()) {
            Toast.makeText(this, "Произошла ошибка", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        shareViewModel = new ViewModelProvider(this).get(EventsInfoShareViewModel.class);
        loadEventDataFromFirebase(currentId);


        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> {
            if (position ==0){
                tab.setText("Информация");
                tab.setIcon(R.drawable.icon_info_event);
            }
            else if (position == 1){
                tab.setText("Гости");
                tab.setIcon(R.drawable.members);
            }
            else if (position == 2){
                tab.setText("Статистика");
                tab.setIcon(R.drawable.statistic_icon);
            }
        })).attach();

        headerView = binding.headerTabs;

        EdgeToEdge.enable(this);
        WindowCompat.enableEdgeToEdge(getWindow());

        ViewCompat.setOnApplyWindowInsetsListener(headerView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            v.getLayoutParams().height += systemBars.top;
            headerView.setOnApplyWindowInsetsListener(null);
            return WindowInsetsCompat.CONSUMED;
        });



    }

    private void loadEventDataFromFirebase(String currentId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app");
        firebaseDatabase.getReference().child("Events").child(currentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Events event =  dataSnapshot.getValue(Events.class);
                            if (event!=null){
                                shareViewModel.setEvent(event, currentId);
                            }
                        }
                        else{
                            Toast.makeText(EventInfoActivity.this,"Произошла ошибка с чтением данных", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EventInfoActivity.this,"Произошла ошибка с чтением данных", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                });
    }

    public static class ViewPagesFragmentAdapter extends FragmentStateAdapter{
        int size;
        public ViewPagesFragmentAdapter(@NonNull FragmentActivity fragmentActivity, int size) {
            super(fragmentActivity);
            this.size = size;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position)
            {
                case 0:
                    return new EventInfoFragment();
                case 1:
                    return new MembersFragment();
                case 2:
                    return new StatisticFragment();
            }
            return new EventInfoFragment();
        }


        @Override
        public int getItemCount() {
            return size;
        }
    }


}