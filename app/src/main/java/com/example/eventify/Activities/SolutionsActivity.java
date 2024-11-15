package com.example.eventify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.eventify.databinding.ActivitySolutionsBinding;
import com.example.eventify.databinding.FragmentCardBinding;

import java.util.ArrayList;

public class SolutionsActivity extends AppCompatActivity {

    public static ArrayList<com.example.eventify.Service> products = new ArrayList<>();
    private ActivitySolutionsBinding servicesBinding;
    private FragmentCardBinding cardBinding;

    private boolean filterOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        servicesBinding = ActivitySolutionsBinding.inflate(getLayoutInflater());
        cardBinding = FragmentCardBinding.inflate(getLayoutInflater());
        setContentView(servicesBinding.getRoot());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.medium_gray));

        servicesBinding.filterBtn.setOnClickListener(v -> {
            if (!filterOn) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(servicesBinding.frame.getId(), SolutionFilterFragment.newInstance("gas","gas"));
                transaction.addToBackStack(null);
                filterOn = true;
                transaction.commit();
            } else {
                filterOn = false;
                getSupportFragmentManager().popBackStack();
            }

        });

        servicesBinding.addBtn.setOnClickListener(v -> {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(servicesBinding.fragmentContent.getId(), com.example.eventify.SolutionFormFragment.newInstance("gas","gas"));
                transaction.addToBackStack(null);
                filterOn = true;
                transaction.commit();

        });



        prepareProductList(products);
        loadServicesListFragment();


    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Optionally add to back stack
                .commit();
    }

    private void loadServicesListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(servicesBinding.frame.getId(), SolutionListFragment.newInstance(products)); // Assuming you have a container for the fragment
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
