package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentServiceDetailsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentServiceDetailsBinding binding;

    private boolean serviceDetails = true;

    public ServiceDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SolutionDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ServiceDetailsFragment newInstance(String param1, String param2) {
        ServiceDetailsFragment fragment = new ServiceDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentServiceDetailsBinding.inflate(inflater, container, false);

        detailsBtnHandler();

        binding.right.setOnClickListener(v -> detailsBtnHandler());


        return binding.getRoot();
    }

    private void detailsBtnHandler() {
        // Access the filter layout
        FrameLayout details = binding.details;

        // Dynamically set the height to 400dp
        ViewGroup.LayoutParams params = details.getLayoutParams();

        // Check if the filter fragment is already shown
        if (serviceDetails) {
            // Begin a fragment transaction to add the filter fragment
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    750, // Desired height in dp
                    getResources().getDisplayMetrics()
            );
            details.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(binding.details.getId(), ServiceDetailsForm.newInstance("gas", "gas"));
            serviceDetails = false;
            transaction.commit();
        } else {
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    750, // Desired height in dp
                    getResources().getDisplayMetrics()
            );
            details.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(binding.details.getId(), PupDetailsForm.newInstance("gas", "gas"));
            serviceDetails = true;
            transaction.commit();
        }
    }
}