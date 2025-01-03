package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentServiceDetailsFormBinding;
import com.example.eventify.models.solutions.Service;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceDetailsForm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceDetailsForm extends Fragment {



    public ServiceDetailsForm() {
        // Required empty public constructor
    }

    FragmentServiceDetailsFormBinding binding;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ServiceDetailsForm.
     */
    // TODO: Rename and change types and number of parameters
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
            Service service = getArguments().getParcelable("service");
            binding.setService(service);
            binding.setLifecycleOwner(this);
        }

        return inflater.inflate(R.layout.fragment_service_details_form, container, false);
    }


}