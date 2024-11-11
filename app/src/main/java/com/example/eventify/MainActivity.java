package com.example.eventify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventify.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ShopApp", "MainActivity onCreate()");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.serviceButton.setOnClickListener(v -> {
            ArrayList<Service> services = new ArrayList<>(); // Populate this list as needed
            ServicesFragment servicesFragment = ServicesFragment.newInstance(services);

            // Begin the fragment transaction
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Replace the entire content with the full-screen ServicesFragment
            transaction.replace(R.id.fragment_container, servicesFragment);
            transaction.addToBackStack(null); // Allows the user to go back to the previous screen
            transaction.commit(); // Apply the transaction
        });
    }
}
