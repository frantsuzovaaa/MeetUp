package com.example.meetup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentEventInfoBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Date;
import java.text.SimpleDateFormat;


public class EventInfoFragment extends Fragment {
    private FragmentEventInfoBinding binding;
    private TextView name_event, date_event, code_word, place;
    private ExtendedFloatingActionButton buttonDelete;
    private EventsInfoShareViewModel shareViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name_event = binding.nameEventFragmentEventInfo;
        date_event = binding.dataFragmentEventInfo;
        code_word = binding.codeWordFragmentEventInfo;
        place = binding.PlaceFragmentEventInfo;
        buttonDelete = binding.buttonDeleteEvent;
        shareViewModel = new ViewModelProvider(requireActivity()).get(EventsInfoShareViewModel.class);
        shareViewModel.getCurrentEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                name_event.setText(event.getNameEvent());
                code_word.setText(event.getCodeWord());
                place.setText(event.getPlace());
                date_event.setText(simpleDateFormat.format(new Date(event.getDataTime())));
            }
        });
        binding.UpdateEventButton.setOnClickListener(v -> {
            UpdateEventDialogFragment dialogFragment = new UpdateEventDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "updateDialogFragmnet");
        });

        shareViewModel.infoToShow().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
            }
        });

        shareViewModel.isNeedToFinish().observe(getViewLifecycleOwner(), isNeedToFinish -> {
            if (isNeedToFinish) {
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        buttonDelete.setOnClickListener(v -> shareViewModel.deleteCurrentEvent());
    }
}