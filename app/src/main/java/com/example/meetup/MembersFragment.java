package com.example.meetup;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentMembersBinding;

import java.util.ArrayList;

public class MembersFragment extends Fragment {
    private FragmentMembersBinding binding;
    private RecyclerView recyclerView;
    private MembersViewModel membersViewModel;
    private AdapterMembers adapterMembers;
    private EventsInfoShareViewModel shareViewModel;
    private ArrayList<Member> cachedMembers = new ArrayList<>();
    private ArrayList<String> cachedMemberIds = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterMembers = new AdapterMembers(getActivity(), new ArrayList<>(), new ArrayList<>());

        recyclerView = binding.recycleViewMembers;
        recyclerView.setLayoutManager( new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapterMembers);

        membersViewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        shareViewModel = new ViewModelProvider(requireActivity()).get(EventsInfoShareViewModel.class);
        membersViewModel.init();
        String currentEventId = shareViewModel.getCurrentEventId().getValue();
        if (currentEventId != null && !currentEventId.isEmpty()) {
            Log.d("DEBUG", "Initial event ID: " + currentEventId);
            membersViewModel.setCurrentEventId(currentEventId);
        }
        shareViewModel.getCurrentEventId().observe(getViewLifecycleOwner(), eventId -> {
            if (eventId != null && !eventId.isEmpty()) {
                Log.d("DEBUG", "Event ID changed: " + eventId);
                membersViewModel.setCurrentEventId(eventId);
            } else {
                Log.e("DEBUG", "Event ID is null or empty");
            }
        });


        binding.buttonAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMembersDialogFragment dialogFragment = new AddMembersDialogFragment();
                dialogFragment.setOnMemberAddedListener(new AddMembersDialogFragment.onMemberAddedListener() {
                    @Override
                    public void onMemberAdded(Member member) {
                        membersViewModel.addMemberWithPhoneCheck(member);
                    }
                });
                dialogFragment.show(getParentFragmentManager(), "addMembersDialog");
            }
        });

        membersViewModel.getMemberIds().observe(getViewLifecycleOwner(), ids -> {
            this.cachedMemberIds = ids;
            updateAdapterIfReady();
        });
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            this.cachedMembers = members;
            updateAdapterIfReady();
        });
        membersViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        membersViewModel.getMemberAddedSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Участник добавлен!", Toast.LENGTH_SHORT).show();
            }
        });

        membersViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonAddMembers.setEnabled(!isLoading);
        });

        adapterMembers.setOnItemClickListener(new AdapterMembers.OnItemClickListener() {
            @Override
            public void onItemClick(Member member, int position) {
            }
        });

        adapterMembers.setOnChangesClickListener(new AdapterMembers.OnChangesClickListener() {
            @Override
            public void onChangesClick(Member member, int position) {
            }
        });

        adapterMembers.setOnOpenQrClickListener(new AdapterMembers.OnOpenQrClickListener() {
            @Override
            public void onOpenQrClick(Member member, int position, String member_id) {
                QrDialogFragment dialog = QrDialogFragment.newInstance(member, member_id);
                dialog.show(getParentFragmentManager(),"QR_dialog");

            }
        });



    }


    private void updateAdapterIfReady() {
        if (cachedMembers.size() == cachedMemberIds.size()) {
            adapterMembers.updateMembers(cachedMembers, cachedMemberIds);
        }
    }

}