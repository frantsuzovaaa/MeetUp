package com.example.meetup;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentUpdateEventDialogBinding;
import com.example.meetup.events.Events;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateEventDialogFragment extends DialogFragment {
    private TextView name_event, date_event, code_word, place;
    FragmentUpdateEventDialogBinding binding;
    FirebaseDatabase firebaseDatabase;
    private EventsInfoShareViewModel shareViewModel;
    Calendar dateAndTime = Calendar.getInstance();

    TimePickerDialog.OnTimeSetListener t;
    DatePickerDialog.OnDateSetListener d;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateEventDialogBinding.inflate(inflater, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app");
        initListeners();

        binding.timeButton.setOnClickListener(v -> setTime());
        binding.dateButton.setOnClickListener(v -> setDate());

        return binding.getRoot();
    }

    public void setTime() {
        new TimePickerDialog(getActivity(), t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    public void setDate() {
        new DatePickerDialog(getActivity(), d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setInitialDateTime() {
        binding.currentUpdateDateTime.setText(DateUtils.formatDateTime(getActivity(),
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
    }

    private void initListeners() {
        t = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dateAndTime.set(Calendar.MINUTE, minute);
                setInitialDateTime();
            }
        };

        d = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateAndTime.set(Calendar.YEAR, year);
                dateAndTime.set(Calendar.MONTH, monthOfYear);
                dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setInitialDateTime();
            }
        };
    }

    public interface onEventUpdateListener{
        void onEventUpdate(Events events);
    }
    private onEventUpdateListener eventUpdateListener;

    public void setOnEventUpdateListener(onEventUpdateListener listener){
        this.eventUpdateListener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name_event = binding.nameUpdateEvent;
        date_event = binding.currentUpdateDateTime;
        code_word = binding.codeUpdateWord;
        place = binding.placeUpdate;

        shareViewModel = new ViewModelProvider(requireActivity()).get(EventsInfoShareViewModel.class); // Перемести выше
        String currentId = shareViewModel.getCurrentEventId().getValue();

        if (currentId == null) {
            Toast.makeText(getActivity(), "Ошибка: событие не найдено", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        shareViewModel.getCurrentEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null){
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                name_event.setText(event.getNameEvent());
                code_word.setText(event.getCodeWord());
                place.setText(event.getPlace());
                date_event.setText(simpleDateFormat.format(new Date(event.getDataTime())));

                dateAndTime.setTimeInMillis(event.getDataTime());
                setInitialDateTime();
            }
        });

        binding.UpdateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentid = shareViewModel.getCurrentEventId().getValue();
                String currentCodeWord = binding.codeUpdateWord.getText().toString();

                if (currentid == null) {
                    Toast.makeText(getActivity(), "Такого мероприятия нет.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.nameUpdateEvent.getText().toString().isEmpty()
                        || binding.placeUpdate.getText().toString().isEmpty()
                        || binding.codeUpdateWord.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show();
                    binding.UpdateEventButton.setEnabled(true);
                    return;
                }
                Query query = firebaseDatabase.
                        getReference("Events").
                        orderByChild("codeWord").
                        equalTo(currentCodeWord);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            boolean codeWordFlag = false;
                            for (DataSnapshot eventSnapshort : snapshot.getChildren()) {
                                String eventId = eventSnapshort.getKey();
                                assert eventId != null;
                                if (!eventId.equals(currentid)) {
                                    codeWordFlag = true;
                                    break;
                                }
                            }

                            if (codeWordFlag) {
                                binding.codeUpdateWord.setError("Кодовое слово уже занято.");
                                Toast.makeText(getActivity(), "Кодовое слово уже занято", Toast.LENGTH_SHORT).show();
                            } else {
                                updateData(currentid, currentCodeWord);
                            }
                        }
                        else{
                            updateData(currentid, currentCodeWord);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "Ошибка проверки кодового слова", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void updateData(String eventId, String codeWord) {
        String nameEvent = binding.nameUpdateEvent.getText().toString();
        String place = binding.placeUpdate.getText().toString();
        long dataTime = dateAndTime.getTimeInMillis();
        String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Events event = new Events(nameEvent, codeWord, place, dataTime, creatorId);

        binding.UpdateEventButton.setEnabled(false);

        firebaseDatabase.getReference()
                .child("Events")
                .child(eventId)
                .setValue(event)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        binding.UpdateEventButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Мероприятие успешно обновлено", Toast.LENGTH_SHORT).show();
                            shareViewModel.updateCurrentEvent(event);
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Ошибка обновления. Попробуйте снова", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}