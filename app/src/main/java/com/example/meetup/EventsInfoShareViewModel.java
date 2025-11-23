package com.example.meetup;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EventsInfoShareViewModel extends ViewModel {
    private final MutableLiveData<String> currentEventId = new MutableLiveData<>();
    private final MutableLiveData<Events> currentEvent = new MutableLiveData<>();

    private final MutableLiveData<String> _infoToShow = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> _isNeedToFinish = new MutableLiveData<>(false);

    public void updateCurrentEvent(Events event) {
        currentEvent.setValue(event);
    }

    public void setEvent(Events event, String eventId) {
        currentEvent.setValue(event);
        currentEventId.setValue(eventId);
    }

    public LiveData<String> getCurrentEventId() {
        return currentEventId;
    }

    public LiveData<Events> getCurrentEvent() {
        return currentEvent;
    }

    public LiveData<String> infoToShow() {
        return _infoToShow;
    }

    public LiveData<Boolean> isNeedToFinish() {
        return _isNeedToFinish;
    }


    void deleteCurrentEvent() {

        String currentId = currentEventId.getValue();
        if (currentId == null || currentId.isEmpty()) {
            Log.d("DEBUG", "deleteEventById: Ошибка удаления, id null или пустой");
            _infoToShow.setValue("Ошибка: ID события не найден");
            return;
        }


        // короче сначала удаляем members от события

        DatabaseReference membersRef = FirebaseDatabase
                .getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Members");

        Query query = membersRef.orderByChild("eventId").equalTo(currentId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                    Log.d("DEBUG", "Deleted member: " + dataSnapshot.getKey());
                }
                Log.d("DEBUG", "Deleted " + snapshot.getChildrenCount() + " members for event: " + currentId);

                // когда удалились все мемберы (причем успешно)
                // мы удаляем само событие
                deleteEvent(currentId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Failed to delete members: " + error.getMessage());
                _infoToShow.setValue("Ошибка при удалении участников");
            }
        });


    }

    private void deleteEvent(String eventId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app");

        DatabaseReference eventRef = firebaseDatabase.getReference("Events").child(eventId);

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    eventRef.removeValue()
                            .addOnSuccessListener(unused -> {
                                _infoToShow.setValue("Мероприятие удалено");
                                _isNeedToFinish.setValue(true);
                            })
                            .addOnFailureListener(e ->
                                    _infoToShow.setValue("Ошибка: " + e.getMessage()));
                } else {
                    _infoToShow.setValue("Событие не найдено");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _infoToShow.setValue("Ошибка чтения: " + error.getMessage());
            }
        });
    }
}
