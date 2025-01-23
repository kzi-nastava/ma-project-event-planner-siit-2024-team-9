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
import com.example.eventify.adapters.ImageListAdapter;
import com.example.eventify.databinding.FragmentServiceDetailsBinding;
import com.example.eventify.models.others.ImageItem;
import com.example.eventify.models.solutions.Service;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceDetailsFragment extends Fragment {

    private ImageListAdapter adapter;

    private boolean isFavorite = true;
    private FragmentServiceDetailsBinding binding;

    private Service showedService;

    private boolean serviceDetails = true;

    public ServiceDetailsFragment() {
        // Required empty public constructor
    }

    public static ServiceDetailsFragment newInstance(Service service) {
        ServiceDetailsFragment fragment = new ServiceDetailsFragment();
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

        binding = FragmentServiceDetailsBinding.inflate(inflater, container, false);

        detailsBtnHandler();

        binding.right.setOnClickListener(v -> detailsBtnHandler());

        if (getArguments() != null) {
            showedService = getArguments().getParcelable("service");
            binding.setService(showedService);
            binding.setLifecycleOwner(this);
        }

        binding.favorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            binding.favorite.setSelected(isFavorite);
        });

        adapter = new ImageListAdapter(requireContext(), showedService.getImages(), getParentFragmentManager());
        binding.recyclerView.setAdapter(adapter);

        binding.star1.setOnClickListener( v -> rate1());
        binding.star2.setOnClickListener( v -> rate2());
        binding.star3.setOnClickListener( v -> rate3());
        binding.star4.setOnClickListener( v -> rate4());
        binding.star5.setOnClickListener( v -> rate5());


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
            binding.right.setText("About us");
            details.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(binding.details.getId(), ServiceDetailsForm.newInstance(getArguments().getParcelable("service")));
            serviceDetails = false;
            transaction.commit();
        } else {
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    750, // Desired height in dp
                    getResources().getDisplayMetrics()
            );
            binding.right.setText("Service");
            details.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(binding.details.getId(), PupDetailsForm.newInstance("gas", "gas"));
            serviceDetails = true;
            transaction.commit();
        }
    }

    private void rate1() {
        resetRatings();
        binding.star1.setSelected(true);
    }

    private void rate2() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
    }

    private void rate3() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
        binding.star3.setSelected(true);
    }

    private void rate4() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
        binding.star3.setSelected(true);
        binding.star4.setSelected(true);
    }

    private void rate5() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
        binding.star3.setSelected(true);
        binding.star4.setSelected(true);
        binding.star5.setSelected(true);
    }


    private void resetRatings() {
        binding.star1.setSelected(false);
        binding.star2.setSelected(false);
        binding.star3.setSelected(false);
        binding.star4.setSelected(false);
        binding.star5.setSelected(false);
    }



}