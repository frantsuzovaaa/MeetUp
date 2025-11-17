package com.example.meetup;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meetup.databinding.FragmentEventsBinding;
import com.example.meetup.databinding.FragmentStatisticBinding;

public class StatisticFragment extends Fragment {

    private FragmentStatisticBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}