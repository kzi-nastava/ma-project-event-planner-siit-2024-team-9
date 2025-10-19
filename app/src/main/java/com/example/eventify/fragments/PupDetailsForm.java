package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentPupDetailsFormBinding;
import com.example.eventify.models.users.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PupDetailsForm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PupDetailsForm extends Fragment {


    FragmentPupDetailsFormBinding binding;

    public PupDetailsForm() {
        // Required empty public constructor
    }

    public static PupDetailsForm newInstance(User owner) {
        PupDetailsForm fragment = new PupDetailsForm();
        Bundle args = new Bundle();
        args.putParcelable("owner", owner);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPupDetailsFormBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            binding.setOwner(getArguments().getParcelable("owner"));
        }

        return binding.getRoot();
    }
}