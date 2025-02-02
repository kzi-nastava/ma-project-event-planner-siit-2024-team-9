package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.databinding.FragmentProductDetailsFormBinding;
import com.example.eventify.databinding.FragmentServiceDetailsFormBinding;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.Product;


public class ProductDetailsForm extends Fragment {

    Product product;

    public ProductDetailsForm() {
        // Required empty public constructor
    }

    FragmentProductDetailsFormBinding binding;


    public static ProductDetailsForm newInstance(Product product) {
        ProductDetailsForm fragment = new ProductDetailsForm();
        Bundle args = new Bundle();
        args.putParcelable("product", product);
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

        binding = FragmentProductDetailsFormBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            product = getArguments().getParcelable("product");
            binding.setService(product);
        }

        setTypes();

        return binding.getRoot();
    }

    private void setTypes() {
        StringBuilder typesInfo = new StringBuilder();
        for (EventType type: product.getEventTypes()) {
            typesInfo.append(type.getName()).append(", ");
        }
        binding.eventTypesInfo.setText(typesInfo.toString());
    }


}