package com.example.meetup.events;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.meetup.events.Events;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventsFragmentViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Events>> _events = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> _eventIds = new MutableLiveData<>();
    private DatabaseReference databaseReference;

    public void init() {
        databaseReference = FirebaseDatabase
                .getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Events");
        _events.setValue(new ArrayList<>());
        _eventIds.setValue(new ArrayList<>());
        setupRealtimeListener();
    }

    private void setupRealtimeListener() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Events> newEvents = new ArrayList<>();
                ArrayList<String> newEventIds = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Events event = dataSnapshot.getValue(Events.class);
                    String eventId = dataSnapshot.getKey();
                    if (event != null) {
                        newEvents.add(event);
                        newEventIds.add(eventId);
                    }
                }
                _events.setValue(newEvents);
                _eventIds.setValue(newEventIds);
                Log.d("DEBUG", "onDataChange: " + newEvents.size() + " events loaded");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Database error: " + error.getMessage());
            }
        });
    }

    public LiveData<ArrayList<String>> eventIds() {
        return _eventIds;
    }

    public void addEvent(Events event) {
        if (databaseReference != null) {
            String key = databaseReference.push().getKey();
            if (key != null) {
                databaseReference.child(key).setValue(event)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("DEBUG", "Event added successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DEBUG", "Failed to add event: " + e.getMessage());
                        });
            }
        }
    }


    public LiveData<ArrayList<Events>> events() {
        return _events;
    }
}