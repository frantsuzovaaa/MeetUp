package com.example.meetup.statistics.ui;

import com.example.meetup.events.viewmodel.EventsInfoSharedViewModel;
import com.example.meetup.members.model.Member;
import com.example.meetup.R;
import com.example.meetup.scanner.model.ScanRecord;
import com.example.meetup.statistics.adapter.StatsTableAdapter;
import com.example.meetup.statistics.model.StatRow;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticFragment extends Fragment {

    private RecyclerView recyclerTable;
    private StatsTableAdapter tableAdapter;

    private AppCompatImageButton btnAll;
    private AppCompatImageButton btnDone;
    private AppCompatImageButton btnErrors;

    private TextView tableTitle;
    private TextView textAll, textDone, textError;

    private final List<StatRow> allRows   = new ArrayList<>();
    private final List<StatRow> doneRows  = new ArrayList<>();
    private final List<StatRow> errorRows = new ArrayList<>();

    private FirebaseDatabase firebaseDatabase;
    private EventsInfoSharedViewModel shareViewModel;

    private enum TableType { ALL, DONE, ERROR }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistic, container, false);

        recyclerTable = view.findViewById(R.id.recyclerHistory);
        tableTitle    = view.findViewById(R.id.titleTable);

        btnAll    = view.findViewById(R.id.all_members);
        btnDone   = view.findViewById(R.id.done_member);
        btnErrors = view.findViewById(R.id.errors_members);

        textAll  = view.findViewById(R.id.text_all_member);
        textDone = view.findViewById(R.id.text_done_member);
        textError = view.findViewById(R.id.text_error_member);

        recyclerTable.setLayoutManager(new LinearLayoutManager(requireContext()));
        tableAdapter = new StatsTableAdapter();
        recyclerTable.setAdapter(tableAdapter);

        firebaseDatabase = FirebaseDatabase.getInstance(
                "https://meetup2-a8e75-default-rtdb.europe-west1.firebasedatabase.app"
        );

        shareViewModel = new ViewModelProvider(requireActivity())
                .get(EventsInfoSharedViewModel.class);


        shareViewModel.getCurrentEventId().observe(
                getViewLifecycleOwner(),
                eventId -> {
                    if (eventId != null && !eventId.isEmpty()) {
                        loadStatsForEvent(eventId);
                    }
                }
        );

        btnAll.setOnClickListener(v -> showTable(TableType.ALL));
        btnDone.setOnClickListener(v -> showTable(TableType.DONE));
        btnErrors.setOnClickListener(v -> showTable(TableType.ERROR));

        showTable(TableType.ALL);

        return view;
    }
    public void refreshData() {
        String eventId = shareViewModel.getCurrentEventId().getValue();
        if (eventId != null && !eventId.isEmpty()) {
            loadStatsForEvent(eventId);
        }
    }

    private void loadStatsForEvent(String eventId) {
        allRows.clear();
        doneRows.clear();
        errorRows.clear();
        firebaseDatabase.getReference("Members")
                .child(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Map<String, Member> membersMap = new HashMap<>();

                        for (DataSnapshot memberSnap : snapshot.getChildren()) {
                            String memberId = memberSnap.getKey();
                            Member member = memberSnap.getValue(Member.class);
                            if (memberId != null && member != null) {
                                membersMap.put(memberId, member);

                                allRows.add(new StatRow(
                                        member.getName(),
                                        member.getNumber()
                                ));
                            }
                        }


                        textAll.setText(String.valueOf(allRows.size()));


                        loadScanHistory(eventId, membersMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки участников: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });




    }

    private void loadScanHistory(String eventId, Map<String, Member> membersMap) {
        firebaseDatabase.getReference("ScanHistory")
                .child(eventId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        doneRows.clear();
                        errorRows.clear();

                        for (DataSnapshot recordSnap : snapshot.getChildren()) {
                            ScanRecord record = recordSnap.getValue(ScanRecord.class);
                            if (record == null) continue;

                            String memberId = record.getMemberId();
                            Member member = membersMap.get(memberId);

                            String name  = (member != null) ? member.getName()   : ("id"+memberId);
                            String phone = (member != null) ? member.getNumber() : "";

                            if (record.isSuccess()) {
                                doneRows.add(new StatRow(
                                        name,
                                        phone
                                ));
                            } else {
                                errorRows.add(new StatRow(
                                        name,
                                        phone
                                ));
                            }
                        }
                        textDone.setText(String.valueOf(doneRows.size()));
                        textError.setText(String.valueOf(errorRows.size()));

                        showTable(TableType.ALL);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки истории: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showTable(TableType type) {
        switch (type) {
            case ALL:
                tableTitle.setText("Всего гостей");
                tableAdapter.update(allRows);
                break;

            case DONE:
                tableTitle.setText("Гости, которые прошли");
                tableAdapter.update(doneRows);
                break;

            case ERROR:
                tableTitle.setText("Ошибки входа");
                tableAdapter.update(errorRows);
                break;
        }
    }


}
