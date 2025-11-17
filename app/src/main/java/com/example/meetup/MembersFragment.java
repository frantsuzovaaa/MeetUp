package com.example.meetup;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meetup.databinding.FragmentEventsBinding;
import com.example.meetup.databinding.FragmentMembersBinding;

public class MembersFragment extends Fragment {
    private FragmentMembersBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}