package com.example.meetup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.meetup.events.Events;

public class EventsInfoShareViewModel extends ViewModel {
    private final MutableLiveData<String> currentEventId = new MutableLiveData<>();
    private final MutableLiveData<Events> currentEvent = new MutableLiveData<>();


    public void setCurrentEventId(String eventId) {
        currentEventId.setValue(eventId);
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

}
