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
import com.example.meetup.databinding.FragmentEventsBinding;
import com.example.meetup.events.EventsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.Executor;


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
        name_event =binding.nameEventFragmentEventInfo;
        date_event = binding.dataFragmentEventInfo;
        code_word = binding.codeWordFragmentEventInfo;
        place = binding.PlaceFragmentEventInfo;
        buttonDelete = binding.buttonDeleteEvent;
        shareViewModel = new ViewModelProvider(requireActivity()).get(EventsInfoShareViewModel.class);
        shareViewModel.getCurrentEvent().observe(getViewLifecycleOwner(),event -> {
            if (event != null){
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                name_event.setText(event.getNameEvent());
                code_word.setText(event.getCodeWord());
                place.setText(event.getPlace());
                date_event.setText(simpleDateFormat.format(new Date(event.getDataTime())));
            }
        });
        String currentId = shareViewModel.getCurrentEventId().getValue();

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String currentId = shareViewModel.getCurrentEventId().getValue();

                if (currentId == null || currentId.isEmpty()) {
                    System.out.println("ОШИБКА: currentId is null or empty");
                    Toast.makeText(getActivity(), "Ошибка: ID события не найден", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app");

                DatabaseReference eventRef = firebaseDatabase.getReference("Events").child(currentId);

                eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            eventRef.removeValue()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getActivity(), "Мероприятие удалено", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), AccountActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);

                                        if (getActivity() != null) {
                                            getActivity().finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Toast.makeText(getActivity(), "Событие не найдено", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "Ошибка чтения: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}