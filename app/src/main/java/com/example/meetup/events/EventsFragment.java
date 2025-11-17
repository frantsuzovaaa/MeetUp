package com.example.meetup.events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.databinding.FragmentEventsBinding;
import com.example.meetup.EventInfoActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private FragmentEventsBinding binding;
    private RecyclerView recyclerView;
    private EventsFragmentViewModel viewModel;
    private AdapterEvents adapterEvents;
    private ArrayList<Events> cachedEvents = new ArrayList<>();
    private ArrayList<String> cachedEventIds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = binding.recycleViewEvent;


        adapterEvents = new AdapterEvents(getActivity(), new ArrayList<>(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapterEvents);

        viewModel = new ViewModelProvider(requireActivity()).get(EventsFragmentViewModel.class);
        viewModel.init();

        viewModel.eventIds().observe(getViewLifecycleOwner(), ids -> {
            this.cachedEventIds = ids;
            updateAdapterIfReady();
        });
        viewModel.events().observe(getViewLifecycleOwner(), events -> {
            this.cachedEvents = events;
            updateAdapterIfReady();
        });

        FloatingActionButton AddButton = binding.buttonAdd;
        AddButton.setOnClickListener(v -> {
            AddEventsDialogFragment dialogFragment = new AddEventsDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "addEventDialog");
        });
        adapterEvents.setOnItemClickListener(new AdapterEvents.OnItemClickListener() {
            @Override
            public void onItemClick(Events event, int position) {
                String id = adapterEvents.getEventId(position);
                Intent intent = new Intent(getActivity(), EventInfoActivity.class);
                intent.putExtra("event_id", id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private  void updateAdapterIfReady(){
        if (!cachedEvents.isEmpty() && !cachedEventIds.isEmpty() &&
                cachedEvents.size() == cachedEventIds.size()){
            adapterEvents.updateEvents(cachedEvents, cachedEventIds);
        }
    }


}