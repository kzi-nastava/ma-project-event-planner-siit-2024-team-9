package com.example.eventify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.eventify.databinding.ActivityServicesBinding;
import com.example.eventify.databinding.FragmentCardBinding;

import java.util.ArrayList;

public class ServicesActivity extends AppCompatActivity {

    public static ArrayList<com.example.eventify.Service> products = new ArrayList<>();
    private ActivityServicesBinding binding;

    private boolean filterOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.medium_gray));

        binding.filterBtn.setOnClickListener(v -> {
            if (!filterOn) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(binding.frame.getId(), ServiceFilterFragment.newInstance("gas","gas"));
                transaction.addToBackStack(null);
                filterOn = true;
                transaction.commit();
            } else {
                filterOn = false;
                getSupportFragmentManager().popBackStack();
            }

        });

        prepareProductList(products);
        loadServicesListFragment();
    }

    private void loadServicesListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.frame.getId(), ServicesListFragment.newInstance(products)); // Assuming you have a container for the fragment
        transaction.commit();

    }

    private void prepareProductList(ArrayList<com.example.eventify.Service> products) {
        products.clear();
        products.add(new com.example.eventify.Service(1L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new com.example.eventify.Service(2L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
        products.add(new com.example.eventify.Service(3L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new com.example.eventify.Service(4L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
        products.add(new com.example.eventify.Service(5L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new com.example.eventify.Service(6L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
        products.add(new com.example.eventify.Service(7L, "Samsung S23 Ultra White", "Description 1", R.drawable.s23));
        products.add(new com.example.eventify.Service(8L, "Samsung S23 Ultra Gray", "Description 2", R.drawable.s23));
    }
}
