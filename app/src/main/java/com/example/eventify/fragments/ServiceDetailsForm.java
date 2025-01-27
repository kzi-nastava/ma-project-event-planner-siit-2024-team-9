package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentServiceDetailsFormBinding;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.Service;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceDetailsForm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceDetailsForm extends Fragment {

    Service service;

    public ServiceDetailsForm() {
        // Required empty public constructor
    }

    FragmentServiceDetailsFormBinding binding;


    public static ServiceDetailsForm newInstance(Service service) {
        ServiceDetailsForm fragment = new ServiceDetailsForm();
        Bundle args = new Bundle();
        args.putParcelable("service", service);
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

        binding = FragmentServiceDetailsFormBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            service = getArguments().getParcelable("service");
            binding.setService(service);
            if (service.getDuration()==0) {
                binding.duration.setVisibility(View.GONE);
                binding.durationInfo.setVisibility(View.GONE);
            } else {
                binding.min.setVisibility(View.GONE);
                binding.minInfo.setVisibility(View.GONE);
                binding.max.setVisibility(View.GONE);
                binding.maxInfo.setVisibility(View.GONE);
            }
        }

        setTypes();

        return binding.getRoot();
    }

    private void setTypes() {
        StringBuilder typesInfo = new StringBuilder();
        for (EventType type: service.getEventTypes()) {
            typesInfo.append(type.getName()).append(", ");
        }
        binding.eventTypesInfo.setText(typesInfo.toString());
    }


}