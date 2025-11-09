package com.example.meetup.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentAddEventsDialogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddEventsDialogFragment extends DialogFragment {
    private FragmentAddEventsDialogBinding binding;
    TextView currentDateTime;
    FirebaseDatabase firebaseDatabase;
    Calendar dateAndTime = Calendar.getInstance();

    TimePickerDialog.OnTimeSetListener t;
    DatePickerDialog.OnDateSetListener d;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddEventsDialogBinding.inflate(inflater, container, false);

        currentDateTime = binding.currentDateTime;

        initListeners();
        setInitialDateTime();
        firebaseDatabase = FirebaseDatabase.getInstance("https://meetup-9708e-default-rtdb.europe-west1.firebasedatabase.app");


        binding.timeButton.setOnClickListener(v -> setTime());
        binding.dateButton.setOnClickListener(v -> setDate());


        return binding.getRoot();
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

    private void setInitialDateTime() {
        currentDateTime.setText(DateUtils.formatDateTime(getActivity(),
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
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
    public  interface onEventAddedListener{
        void onEventAdded(Events events);
    }
    private onEventAddedListener eventListener;

    public void setOnEventAddedListener(onEventAddedListener listener) {
        this.eventListener = listener;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.addEventButton.setOnClickListener(v -> {
            binding.codeWord.setError(null);
            binding.addEventButton.setEnabled(false);
            binding.codeWord.setBackgroundTintList(null);
            String codeWord = binding.codeWord.getText().toString();
            if (binding.nameEvent.getText().toString().isEmpty()
                    || binding.place.getText().toString().isEmpty()
                    || binding.codeWord.getText().toString().isEmpty()){
                Toast.makeText(getActivity(), "Поля не могут быть пустыми",Toast.LENGTH_SHORT).show();
                binding.addEventButton.setEnabled(true);
                return;
            }
            Query query = firebaseDatabase.
                    getReference("Events").
                    orderByChild("codeWord").
                    equalTo(codeWord);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        binding.codeWord.setError("Код уже занят.");
                        binding.addEventButton.setEnabled(true);
                    }
                    else{
                        saveData(codeWord);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Произошла ошибка. Попробуйте заново.", Toast.LENGTH_SHORT).show();
                    binding.addEventButton.setEnabled(true);
                }
            });


        });
    }

    private void saveData(String codeWord){
            String nameEvent = binding.nameEvent.getText().toString();
            String place = binding.place.getText().toString();
            long dataTime = dateAndTime.getTimeInMillis();
            String eventId = firebaseDatabase.getReference().push().getKey();
            String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Events event = new Events(nameEvent, codeWord, place, dataTime, creatorId);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        assert eventId != null;
        firebaseDatabase.getReference()
                    .child("Events")
                    .child(eventId)
                    .setValue(event)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getActivity(),  "Мероприятие успешно сохранилось", Toast.LENGTH_SHORT).show();
                                binding.addEventButton.setEnabled(true);
                                if (eventListener!= null){
                                    eventListener.onEventAdded(event);
                                }
                                dismiss();
                            }
                            else{
                                Toast.makeText(getActivity(), "Возникла ошибка. Попробуйте снова", Toast.LENGTH_SHORT).show();
                                binding.addEventButton.setEnabled(true);

                            }
                        }
                    });

    }
}

