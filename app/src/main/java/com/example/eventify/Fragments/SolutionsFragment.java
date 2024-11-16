package com.example.eventify.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventify.Model.Service;
import com.example.eventify.R;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.databinding.FragmentRegisterBinding;
import com.example.eventify.databinding.FragmentSolutionsBinding;

import java.util.ArrayList;

public class SolutionsFragment extends Fragment {


    public static ArrayList<Service> products = new ArrayList<>();
    private FragmentSolutionsBinding servicesBinding;
    private FragmentCardBinding cardBinding;

    private boolean filterOn = false;

    public SolutionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        servicesBinding = FragmentSolutionsBinding.inflate(inflater, container, false);
        cardBinding = FragmentCardBinding.inflate(inflater, container, false);

        servicesBinding.filterBtn.setOnClickListener(v -> filterBtnHandler());
        servicesBinding.addBtn.setOnClickListener(v -> addBtnHandler());

        prepareProductList(products);
        loadServicesListFragment();

        return servicesBinding.getRoot();
    }

    private void filterBtnHandler() {
        if (!filterOn) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(servicesBinding.frameSolutions.getId(), SolutionFilterFragment.newInstance("gas","gas"));
            transaction.addToBackStack(null);
            filterOn = true;
            transaction.commit();

        } else {
            filterOn = false;
            getChildFragmentManager().popBackStack();
        }
    }

    private void loadServicesListFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(servicesBinding.frameSolutions.getId(), SolutionListFragment.newInstance(products));
        transaction.commit();

    }

    private void addBtnHandler() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(servicesBinding.frameSolutions.getId(), SolutionFormFragment.newInstance("gas","gas"));
        transaction.addToBackStack(null);
        filterOn = true;
        transaction.commit();
    }

    private void prepareProductList(ArrayList<Service> products) {
        products.clear();
        products.add(new Service(1L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new Service(2L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
        products.add(new Service(3L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new Service(4L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
        products.add(new Service(5L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new Service(6L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
        products.add(new Service(7L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new Service(8L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
    }

}