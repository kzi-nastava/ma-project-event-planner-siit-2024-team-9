package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.eventify.adapters.ServiceListAdapter;
import com.example.eventify.databinding.FragmentServicesBinding;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.services.ServiceService;

import java.util.ArrayList;

public class ServicesFragment extends Fragment {

    private static final String ARG_PARAM = "param";

    private ArrayList<Service> mProducts;

    public static ArrayList<Service> products = new ArrayList<>();
    private FragmentServicesBinding servicesBinding;
    private FragmentCardBinding cardBinding;

    private ServiceListAdapter adapter;

    private boolean filterOn = false;

    public ServicesFragment() {
        // Required empty public constructor
    }

    ServiceService service = ServiceService.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        servicesBinding = FragmentServicesBinding.inflate(inflater, container, false);

        // Set up button click listeners
        servicesBinding.filterBtn.setOnClickListener(v -> filterBtnHandler());
        servicesBinding.addBtn.setOnClickListener(v -> addBtnHandler());

        // Initialize the product list (Ensure mProducts is not null)
        mProducts = service.getAll();

        // Initialize the adapter and bind it to the RecyclerView
        adapter = new ServiceListAdapter(requireContext(), mProducts, getParentFragmentManager());
        servicesBinding.recyclerView.setAdapter(adapter);

        return servicesBinding.getRoot();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceService service = ServiceService.getInstance();
    }

    private void filterBtnHandler() {
        // Access the filter layout
        FrameLayout filterLayout = servicesBinding.filter;

        // Dynamically set the height to 400dp
        ViewGroup.LayoutParams params = filterLayout.getLayoutParams();

        // Check if the filter fragment is already shown
        if (!filterOn) {
            // Begin a fragment transaction to add the filter fragment
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    280, // Desired height in dp
                    getResources().getDisplayMetrics()
            );
            filterLayout.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(servicesBinding.filter.getId(), SolutionFilterFragment.newInstance("gas", "gas"));
            transaction.addToBackStack("services");
            filterOn = true;
            transaction.commit();
        } else {
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    0, // Desired height in dp
                    getResources().getDisplayMetrics()
            );
            filterLayout.setLayoutParams(params);
            // If the filter fragment is already shown, pop it from the back stack
            filterOn = false;
            getChildFragmentManager().popBackStack();
        }
    }



    private void addBtnHandler() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(servicesBinding.fragmentContent.getId(), ServiceFormFragment.newInstance(new Service()));
        transaction.addToBackStack("services");
        transaction.commit();
    }



}