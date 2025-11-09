package com.example.meetup.events;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private FragmentEventsBinding binding;
    private RecyclerView recyclerView;
    private EventsFragmentViewModel viewModel;
    private AdapterEvents adapterEvents;

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


        adapterEvents = new AdapterEvents(getActivity(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapterEvents);

        viewModel = new ViewModelProvider(requireActivity()).get(EventsFragmentViewModel.class);
        viewModel.init();

        viewModel.events().observe(getViewLifecycleOwner(), events -> {
            Log.d("DEBUG", "onViewCreated: new data received, size: " + events.size());
            adapterEvents.updateEvents(events);
        });

        FloatingActionButton AddButton = binding.buttonAdd;
        AddButton.setOnClickListener(v -> {
            AddEventsDialogFragment dialogFragment = new AddEventsDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "addEventDialog");
        });
        adapterEvents.setOnItemClickListener(new AdapterEvents.OnItemClickListener() {
            @Override
            public void onItemClick(Events event, int position) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}