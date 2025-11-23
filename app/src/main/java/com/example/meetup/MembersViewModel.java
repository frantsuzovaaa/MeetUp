package com.example.meetup;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MembersViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Member>> _members = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> _memberIds = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _memberAddedSuccess = new MutableLiveData<>();

    private DatabaseReference databaseReference;
    private String currentEventId;
    private ValueEventListener currentEventListener;

    public void init() {
        databaseReference = FirebaseDatabase
                .getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Members");
        _members.setValue(new ArrayList<>());
        _memberIds.setValue(new ArrayList<>());
        _isLoading.setValue(false);
        _memberAddedSuccess.setValue(false);
    }

    public void setCurrentEventId(String eventId) {
        this.currentEventId = eventId;

        if (currentEventListener != null) {
            databaseReference.removeEventListener(currentEventListener);
            currentEventListener = null;
        }

        if (eventId != null && !eventId.isEmpty()) {
            setupRealtimeListenerForEvent(eventId);
        } else {
            _members.setValue(new ArrayList<>());
            _memberIds.setValue(new ArrayList<>());
        }
    }


    private void setupRealtimeListenerForEvent(String eventId) {

        Query query = databaseReference.orderByChild("eventId").equalTo(eventId);

        currentEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Member> newMembers = new ArrayList<>();
                ArrayList<String> newMemberIds = new ArrayList<>();

                Log.d("DEBUG", "Firebase returned " + snapshot.getChildrenCount() + " members for event: " + eventId);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Member member = dataSnapshot.getValue(Member.class);
                    String memberId = dataSnapshot.getKey();

                    if (member != null) {
                        Log.d("DEBUG", "Found member: " + member.getName() + " with event_id: " + member.getEventId());
                        newMembers.add(member);
                        newMemberIds.add(memberId);
                    }
                }

                _members.setValue(newMembers);
                _memberIds.setValue(newMemberIds);
                Log.d("DEBUG", "Members loaded for event " + eventId + ": " + newMembers.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Database error: " + error.getMessage());
                _errorMessage.setValue("Ошибка загрузки участников: " + error.getMessage());
            }
        };

        query.addValueEventListener(currentEventListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentEventListener != null) {
            databaseReference.removeEventListener(currentEventListener);
        }
    }

    public void addMemberWithPhoneCheck(Member member) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _memberAddedSuccess.setValue(false);

        Query query = databaseReference
                .orderByChild("event_id")
                .equalTo(member.getEventId());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean phoneExists = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Member existingMember = dataSnapshot.getValue(Member.class);
                    if (existingMember != null && existingMember.getNumber().equals(member.getNumber())) {
                        phoneExists = true;
                        break;
                    }
                }

                _isLoading.setValue(false);
                if (phoneExists) {
                    _errorMessage.setValue("Этот номер телефона уже зарегистрирован для данного мероприятия");
                    Log.e("DEBUG", "Phone number already exists: " + member.getNumber());
                } else {
                    addMemberToFirebase(member);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Ошибка проверки номера: " + error.getMessage());
                Log.e("DEBUG", "Phone check failed: " + error.getMessage());
            }
        });
    }

    private void addMemberToFirebase(Member member) {
        _isLoading.setValue(true);

        String key = databaseReference.push().getKey();
        if (key != null) {
            databaseReference.child(key).setValue(member)
                    .addOnSuccessListener(aVoid -> {
                        _isLoading.setValue(false);
                        _memberAddedSuccess.setValue(true);
                        Log.d("DEBUG", "Member added successfully: " + member.getName());
                    })
                    .addOnFailureListener(e -> {
                        _isLoading.setValue(false);
                        _errorMessage.setValue("Ошибка сохранения: " + e.getMessage());
                        Log.e("DEBUG", "Failed to add member: " + e.getMessage());
                    });
        } else {
            _isLoading.setValue(false);
            _errorMessage.setValue("Ошибка генерации ID участника");
        }
    }

    public LiveData<ArrayList<Member>> getMembers() {
        return _members;
    }

    public LiveData<ArrayList<String>> getMemberIds() {
        return _memberIds;
    }

    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    public LiveData<Boolean> getMemberAddedSuccess() {
        return _memberAddedSuccess;
    }
}